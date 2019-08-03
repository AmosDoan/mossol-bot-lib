package net.mossol.bot.storage.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.repository.LocationInfoMongoDBRepository;
import net.mossol.bot.service.MenuServiceHandler.FoodType;
import net.mossol.bot.storage.MenuStorageService;
import net.mossol.bot.util.MossolJsonUtil;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Change;
import com.linecorp.centraldogma.common.PushResult;
import com.linecorp.centraldogma.common.Revision;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MenuStorageServiceImpl implements MenuStorageService {

    private volatile Map<String, LocationInfo> koreaMenu;
    private volatile Map<String, LocationInfo> japanMenu;
    private volatile Map<String, LocationInfo> drinkMenu;

    private static final Map<String, LocationInfo> koreaDefaultCandidate =
            new HashSet<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비", "버거킹", "숯불정식", "돈돈정",
                                        "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
                                        "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜", "화수목"))
                    .stream()
                    .collect(Collectors.toMap(e -> e, e -> new LocationInfo(e, -1, -1)));
    private static final Map<String, LocationInfo> japanDefaultCandidate =
            new HashSet<>(Arrays.asList("규카츠", "스시", "라멘", "돈카츠", "꼬치", "덴뿌라", "쉑쉑버거", "카레"))
                    .stream()
                    .collect(Collectors.toMap(e -> e, e -> new LocationInfo(e, -1, -1)));
    private static final Map<String, LocationInfo> drinkDefaultCandidate =
            new HashSet<>(Arrays.asList("하누비노"))
                    .stream()
                    .collect(Collectors.toMap(e -> e, e -> new LocationInfo(e, -1, -1)));

    @Resource
    private CentralDogma centralDogma;

    @Resource
    private Watcher<Map<String, LocationInfo>> japanMenuWatcher;

    @Resource
    private Watcher<Map<String, LocationInfo>> drinkMenuWatcher;

    @Resource
    private LocationInfoMongoDBRepository locationInfoMongoDBRepository;

    @PostConstruct
    private void init() throws InterruptedException {
        japanMenu = japanDefaultCandidate;
        drinkMenu = drinkDefaultCandidate;

        japanMenuWatcher.watch((revision, menu) -> {
            if (menu == null)  {
                log.warn("Japan Menu Watch Failed");
                return;
            }
            log.info("Japan Menu Updated : " + menu);
            japanMenu = menu;
        });

        try {
            japanMenuWatcher.awaitInitialValue(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Failed fetch Japan Menu from Central Dogma; Set the Default Menu");
        }

        drinkMenuWatcher.watch((revision, menu) -> {
            if (menu == null)  {
                log.warn("Drink Menu Watch Failed");
                return;
            }
            log.info("Drink Menu Updated : " + menu);
            drinkMenu = menu;
        });

        try {
            drinkMenuWatcher.awaitInitialValue(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Failed fetch Drink Menu from Central Dogma; Set the Default Menu", e);
        }

        koreaMenu = locationInfoMongoDBRepository.findAll().stream()
                                                 .collect(Collectors.toMap(LocationInfo::getTitle,
                                                                           Function.identity()));

        if (koreaMenu == null) {
            log.info("Failed fetch Korea Menu from MongoDB; Set the Default Menu");
            koreaMenu = koreaDefaultCandidate;
        }
    }

    @Override
    public Map<String, LocationInfo> getMenu(FoodType type) {
        switch (type) {
            case JAPAN_FOOD:
                return japanMenu;
            case KOREA_FOOD:
                return koreaMenu;
            case DRINK_FOOD:
                return drinkMenu;
            default:
                return koreaMenu;
        }
    }

    @Override
    public List<LocationInfo> getMenu() {
        return locationInfoMongoDBRepository.findAll();
    }

    private void removeMenuFromMongoDB(LocationInfo locationInfo) {
        final String locationId = locationInfo.getId();
        log.debug("delete location; id <{}> locationInfo <{}> ", locationId, locationInfo);
        locationInfoMongoDBRepository.deleteById(locationId);
    }

    @Override
    public boolean removeMenu(FoodType foodType, String food) {
        Map<String, LocationInfo> menu = getMenu(foodType);
        if (!menu.containsKey(food)) {
            return false;
        }

        LocationInfo locationInfo = menu.remove(food);

        if (foodType == FoodType.KOREA_FOOD) {
            removeMenuFromMongoDB(locationInfo);
        } else {
            addMenuToCentralDogma(menu, foodType);
        }

        return true;
    }

    @Override
    public boolean removeMenu(FoodType foodType, LocationInfo locationInfo) {
        Map<String, LocationInfo> menu = getMenu(foodType);
        String food = locationInfo.getTitle();
        if (!menu.containsKey(food)) {
            return false;
        }

        LocationInfo removedLocation = menu.remove(food);
        if (locationInfo.getId() != removedLocation.getId()) {
            log.warn("Target locationInfo and stored locationInfo id is not same; "
                     + "targetLocationInfo<{}>;removedLocationInfo<{}>", locationInfo, removedLocation);
        }

        removeMenuFromMongoDB(locationInfo);
        return true;
    }

    private String addMenuToMongoDB(LocationInfo locationInfo) {
        LocationInfo ret = locationInfoMongoDBRepository.insert(locationInfo);
        log.debug("add location; id <{}> locationInfo <{}> ", ret.getId(), ret);
        return ret.getId();
    }

    private void addMenuToCentralDogma(Map<String, LocationInfo> menu, FoodType foodType) {
        String jsonMenu = MossolJsonUtil.writeJsonString(menu.entrySet().stream()
                                                             .map(Map.Entry::getValue)
                                                             .collect(Collectors.toList()));
        if (jsonMenu == null) {
            return;
        }

        String jsonPath = null;
        switch(foodType) {
            case JAPAN_FOOD:
                jsonPath = "/japanMenu.json";
                break;
            case DRINK_FOOD:
                jsonPath = "/drinkMenu.json";
                break;
        }

        log.info("Update Menu to {} : MENU{}", jsonPath, jsonMenu);
        CompletableFuture<PushResult> future = null;
        try {
            future = centralDogma.push("mossol_menu", "main", Revision.HEAD,
                                       "Add new Menu",
                                       Change.ofJsonUpsert(jsonPath, jsonMenu));
        } catch (Exception e) {
            log.debug("Menu Update Failed : {} {} ", jsonPath, e);
        }

        future.whenComplete((result, e) -> {
            if (e != null) {
                log.debug("Menu Update Failed : {} ", e.getCause());
                return;
            }
            log.info("Pushed a commit {} at {}", result.revision(), result.whenAsText());
        });
    }

    @Override
    public boolean addMenu(FoodType foodType, String food) {
        Map<String, LocationInfo> menu = getMenu(foodType);
        if (menu.containsKey(food)) {
            return false;
        }

        LocationInfo locationInfo = new LocationInfo(food, -1, -1);
        menu.put(food, locationInfo);

        if (foodType == FoodType.KOREA_FOOD) {
            addMenuToMongoDB(locationInfo);
        } else {
            addMenuToCentralDogma(menu, foodType);
        }

        return true;
    }

    @Override
    public String addMenu(FoodType foodType, LocationInfo locationInfo) {
        Map<String, LocationInfo> menu = getMenu(foodType);
        String food = locationInfo.getTitle();

        if (menu.containsKey(food)) {
            return null;
        }

        menu.put(food, locationInfo);
        return addMenuToMongoDB(locationInfo);
    }

    @Override
    public boolean updateMenu(String locationId, LocationInfo locationInfo) {
        if (!locationInfoMongoDBRepository.existsById(locationId)) {
            return false;
        }

        locationInfoMongoDBRepository.save(locationInfo);
        return false;
    }
}
