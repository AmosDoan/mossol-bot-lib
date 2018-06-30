package net.mossol.service.Impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mossol.context.MenuContextUtil;
import net.mossol.model.MenuInfo;
import net.mossol.service.MenuServiceHandler;

import com.linecorp.centraldogma.client.Watcher;

import javafx.util.Pair;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class MenuServiceHandlerImpl implements MenuServiceHandler {
    private static final Logger logger = LoggerFactory.getLogger(MenuServiceHandlerImpl.class);
    private static final String menuFormat = "메뉴 리스트는 다음과 같아요 멍\n%s";
    private static final String selectFormat = "멍멍 %s 안먹으면 가서 깨뭅니다";
    private static final String addFormat = "멍멍 %s 추가합니다";
    private static final String removeFormat = "멍멍 %s 가지마세요! 가면 깨뭅니다";
    private static final String removeFail = "멍멍 그런 메뉴 없어요";
    private static final String alreadyExistMenu = "멍멍 이미 있는 곳이에요";
    private static final String addFail = "왈왈! 추가 실패!!";

    private volatile Map<String, MenuInfo> koreaMenu;
    private volatile Map<String, MenuInfo> japanMenu;
    private volatile Map<String, MenuInfo> drinkMenu;

    private static final Map<String, MenuInfo> drinkDefaultCandidate =
        new HashSet<>(Arrays.asList("사랑애수산", "더블린"))
            .stream()
            .collect(Collectors.toMap(e -> e, e -> new MenuInfo(e, -1, -1)));
    private static final Map<String, MenuInfo> koreaDefaultCandidate =
        new HashSet<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비", "버거킹", "숯불정식", "돈돈정",
                                    "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
                                    "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜", "화수목"))
            .stream()
            .collect(Collectors.toMap(e -> e, e -> new MenuInfo(e, -1, -1)));
    private static final Map<String, MenuInfo> japanDefaultCandidate =
        new HashSet<>(Arrays.asList("규카츠", "스시", "라멘", "돈카츠", "꼬치", "덴뿌라", "쉑쉑버거", "카레"))
            .stream()
            .collect(Collectors.toMap(e -> e, e -> new MenuInfo(e, -1, -1)));

    @Autowired
    private Watcher<Map<String, MenuInfo>> japanMenuWatcher;

    @Autowired
    private Watcher<Map<String, MenuInfo>> koreaMenuWatcher;

    @Autowired
    private Watcher<Map<String, MenuInfo>> drinkMenuWatcher;

    private final Random random = new Random();

    @PostConstruct
    private void init() throws InterruptedException {
        japanMenu = japanDefaultCandidate;
        koreaMenu = koreaDefaultCandidate;

        japanMenuWatcher.watch((revision, menu) -> {
            if (menu == null)  {
                logger.warn("Japan Menu Watch Failed");
                return;
            }
            logger.info("Japan Menu Updated : " + menu);
            japanMenu = menu;
        });

        try {
            japanMenuWatcher.awaitInitialValue(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Failed fetch Japan Menu from Central Dogma; Set the Default Menu");
        }

        koreaMenuWatcher.watch((revision, menu) -> {
            if (menu == null)  {
                logger.warn("Korea Menu Watch Failed");
                return;
            }
            logger.info("Korea Menu Updated : " + menu);
            koreaMenu = menu;
        });

        try {
            koreaMenuWatcher.awaitInitialValue(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Failed fetch Korea Menu from Central Dogma; Set the Default Menu", e);
        }

        drinkMenuWatcher.watch((revision, menu) -> {
            if (menu == null)  {
                logger.warn("Drink Menu Watch Failed");
                return;
            }
            logger.info("Drink Menu Updated : " + menu);
            drinkMenu = menu;
        });

        try {
            drinkMenuWatcher.awaitInitialValue(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Failed fetch Drink Menu from Central Dogma; Set the Default Menu", e);
        }
    }

    private Map<String, MenuInfo> selectMenuType(FoodType type) {
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
    public String getMenu(FoodType type) {
        logger.debug("getMenu");
        Set<String> menu = selectMenuType(type).entrySet().stream().map(e -> e.getValue().getTitle())
                                               .collect(Collectors.toSet());
        String msg = String.format(menuFormat, String.join("\n", menu));
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<String, MenuInfo> selectMenu(FoodType type) {
        logger.debug("selectMenu");
        Map<String, MenuInfo> menu = selectMenuType(type);
        int idx = (random.nextInt() & Integer.MAX_VALUE)% menu.size();

        Iterator<String> iterator = menu.keySet().iterator();
        for (int i = 0; i < idx + 1; i ++) {
            iterator.next();
        }

        String select = iterator.next();
        logger.debug("Selected Menu : {}", select);
        return new Pair(String.format(selectFormat, select), menu.get(select));
    }

    @Override
    public String addMenu(String food, FoodType type) {
        logger.debug("addMenu : " + food);
        if (food.isEmpty()) {
            return addFail;
        }

        Map<String, MenuInfo> menu = selectMenuType(type);
        if (menu.containsKey(food)) {
            return alreadyExistMenu;
        }

        menu.put(food, new MenuInfo(food, -1, -1));
        String msg = String.format(addFormat, food);
        logger.debug("DEBUG : {}", msg);

        MenuContextUtil.updateMenu(menu, type);

        return msg;
    }

    @Override
    public String removeMenu(String food, FoodType type) {
        logger.debug("remove Menu : " + food);
        if (food.isEmpty()) {
            return removeFail;
        }

        Map<String, MenuInfo> menu = selectMenuType(type);
        if (!menu.containsKey(food)) {
            return removeFail;
        }

        menu.remove(food);
        String msg = String.format(removeFormat, food);
        logger.debug("DEBUG : {}", msg);

        MenuContextUtil.updateMenu(menu, type);

        return msg;
    }
}
