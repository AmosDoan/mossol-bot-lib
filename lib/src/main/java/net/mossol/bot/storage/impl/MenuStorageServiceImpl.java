package net.mossol.bot.storage.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.model.MenuType;
import net.mossol.bot.repository.LocationInfoMongoDBRepository;
import net.mossol.bot.storage.MenuStorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MenuStorageServiceImpl implements MenuStorageService {

    private volatile Map<String, LocationInfo> koreaMenu;
    private volatile Map<String, LocationInfo> japanMenu;
    private volatile Map<String, LocationInfo> koreaDrinkMenu;

    private static final Map<String, LocationInfo> koreaDefaultMenu =
            new HashSet<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비", "버거킹", "숯불정식", "돈돈정",
                                        "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
                                        "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜", "화수목"))
                    .stream()
                    .collect(Collectors.toMap(e -> e, e -> new LocationInfo(e, -1, -1, MenuType.KOREA_MENU)));
    private static final Map<String, LocationInfo> japanDefaultMenu =
            new HashSet<>(Arrays.asList("규카츠", "스시", "라멘", "돈카츠", "꼬치", "덴뿌라", "쉑쉑버거", "카레"))
                    .stream()
                    .collect(Collectors.toMap(e -> e, e -> new LocationInfo(e, -1, -1, MenuType.JAPAN_MENU)));
    private static final Map<String, LocationInfo> koreaDefaultDrinkMenu =
            new HashSet<>(Arrays.asList("하누비노"))
                    .stream()
                    .collect(Collectors.toMap(e -> e, e -> new LocationInfo(e, -1, -1, MenuType.KOREA_DRINK_MENU)));

    @Resource
    private LocationInfoMongoDBRepository locationInfoMongoDBRepository;

    private Map<String, LocationInfo> loadMenuByType(List<LocationInfo> menuList, MenuType menuType) {
        Map<String, LocationInfo> menu =
                menuList.stream().filter(e -> e.getType() == menuType)
                        .collect(Collectors.toMap(LocationInfo::getTitle, Function.identity()));

        if (CollectionUtils.isEmpty(menu)) {
            log.info("Failed fetch menuType <{}> from MongoDB; Set the Default Menu", menuType);
            return null;
        } else {
            log.info("Success to fetch menuType <{}> from MongoDB <{}>", menuType, menu);
            return menu;
        }
    }

    @PostConstruct
    private void init() throws InterruptedException {
        final List<LocationInfo> menu = locationInfoMongoDBRepository.findAll();

        if (CollectionUtils.isEmpty(menu)) {
            log.warn("Failed fetch Menu from MongoDB; Set the Default Menu");
            koreaMenu = koreaDefaultMenu;
            japanMenu = japanDefaultMenu;
            koreaDrinkMenu = koreaDefaultDrinkMenu;
            return;
        }

        koreaMenu = loadMenuByType(menu, MenuType.KOREA_MENU);
        if (koreaMenu == null) {
            koreaMenu = koreaDefaultMenu;
        }

        if (japanMenu == null) {
            japanMenu = japanDefaultMenu;
        }

        if (koreaDrinkMenu == null) {
            koreaDrinkMenu = koreaDefaultDrinkMenu;
        }
    }

    @Override
    public Map<String, LocationInfo> getMenuList(MenuType type) {
        switch (type) {
            case JAPAN_MENU:
                return japanMenu;
            case KOREA_MENU:
                return koreaMenu;
            case KOREA_DRINK_MENU:
                return koreaDrinkMenu;
            default:
                return koreaMenu;
        }
    }

    @Override
    public List<LocationInfo> getAllLocationInfoList() {
        return locationInfoMongoDBRepository.findAll();
    }

    private void removeLocationInfoFromMongoDB(LocationInfo locationInfo) {
        final String locationId = locationInfo.getId();
        log.debug("delete location; id <{}> locationInfo <{}> ", locationId, locationInfo);
        locationInfoMongoDBRepository.deleteById(locationId);
    }

    @Override
    public boolean removeMenu(MenuType menuType, String food) {
        Map<String, LocationInfo> menu = getMenuList(menuType);
        if (!menu.containsKey(food)) {
            return false;
        }

        LocationInfo locationInfo = menu.remove(food);
        removeLocationInfoFromMongoDB(locationInfo);

        return true;
    }

    @Override
    public boolean removeLocationInfo(LocationInfo locationInfo) {
        Map<String, LocationInfo> menuList = getMenuList(locationInfo.getType());
        String menu = locationInfo.getTitle();
        if (!menuList.containsKey(menu)) {
            return false;
        }

        LocationInfo removedLocation = menuList.remove(menu);
        if (locationInfo.getId() != removedLocation.getId()) {
            log.warn("Target locationInfo and stored locationInfo id is not same; "
                     + "targetLocationInfo<{}>;removedLocationInfo<{}>", locationInfo, removedLocation);
        }

        removeLocationInfoFromMongoDB(locationInfo);
        return true;
    }

    private String addLocationInfoToMongoDB(LocationInfo locationInfo) {
        LocationInfo ret = locationInfoMongoDBRepository.insert(locationInfo);
        log.debug("add location; id <{}> locationInfo <{}> ", ret.getId(), ret);
        return ret.getId();
    }

    @Override
    public boolean addMenu(MenuType menuType, String food) {
        Map<String, LocationInfo> menu = getMenuList(menuType);
        if (menu.containsKey(food)) {
            return false;
        }

        final LocationInfo locationInfo = new LocationInfo(food, -1, -1, menuType);
        menu.put(food, locationInfo);
        addLocationInfoToMongoDB(locationInfo);
        return true;
    }

    @Override
    public String addLocationInfo(LocationInfo locationInfo) {
        Map<String, LocationInfo> menu = getMenuList(locationInfo.getType());
        String food = locationInfo.getTitle();

        if (menu.containsKey(food)) {
            return null;
        }

        menu.put(food, locationInfo);
        return addLocationInfoToMongoDB(locationInfo);
    }

    @Override
    public boolean updateLocationInfo(String locationId, LocationInfo locationInfo) {
        if (!locationInfoMongoDBRepository.existsById(locationId)) {
            return false;
        }

        locationInfoMongoDBRepository.save(locationInfo);
        return false;
    }
}
