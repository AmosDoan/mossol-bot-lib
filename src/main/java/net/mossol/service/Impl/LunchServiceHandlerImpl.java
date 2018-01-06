package net.mossol.service.Impl;

import com.linecorp.centraldogma.client.Watcher;
import net.mossol.context.MenuContextUtil;
import net.mossol.service.LunchServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class LunchServiceHandlerImpl implements LunchServiceHandler {
    private final static Logger logger = LoggerFactory.getLogger(LunchServiceHandlerImpl.class);
    private final String menuFormat = "메뉴 리스트는 다음과 같아요 멍\n%s";
    private final String selectFormat = "멍멍 %s 안먹으면 가서 깨뭅니다";
    private final String addFormat = "멍멍 %s 추가합니다";
    private final String removeFormat = "멍멍 %s 가지마세요! 가면 깨뭅니다";
    private final String removeFail = "멍멍 그런 메뉴 없어요";
    private final String alreadyExistMenu = "멍멍 이미 있는 곳이에요";
    private final String addFail = "왈왈! 추가 실패!!";

    private List<String> koreaMenu;
    private List<String> japanMenu;
    private static final List<String> koreaDefaultCandidate =
        new ArrayList<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비", "버거킹", "숯불정식", "돈돈정",
                                      "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
                                      "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜", "화수목"));
    private static final List<String> japanDefaultCandidate =
        new ArrayList<>(Arrays.asList("규카츠", "스시", "라멘", "돈카츠", "꼬치", "덴뿌라", "쉑쉑버거", "카레"));

    @Autowired
    private Watcher<List<String>> japanMenuWatcher;

    @Autowired
    private Watcher<List<String>> koreaMenuWatcher;

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
            logger.error("Failed fetch Korea Menu from Central Dogma; Set the Default Menu");
        }
    }

    private List<String> selectMenuType(FoodType type) {
        if (type == FoodType.KOREA_FOOD) {
            logger.debug("KOREA_FOOD");
            return koreaMenu;
        } else if (type == FoodType.JAPAN_FOOD) {
            logger.debug("JAPAN_FOOD");
            return japanMenu;
        } else {
            return koreaMenu;
        }
    }

    private int isExistingMenu(String food, List<String> menu) {
        int i;
        for (i = 0; i < menu.size(); i++) {
            String element = menu.get(i);
            if (element.equals(food)) {
                return i;
            }
        }
        return i;
    }

    @Override
    public String getMenu(FoodType type) {
        logger.debug("getMenu");
        List<String> menu = selectMenuType(type);
        String msg = String.format(menuFormat, String.join("\n", menu));
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String selectMenu(FoodType type) {
        logger.debug("selectMenu");
        List<String> menu = selectMenuType(type);
        int idx = (random.nextInt() & Integer.MAX_VALUE)% menu.size();
        String select = menu.get(idx);
        String msg = String.format(selectFormat, select);
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String addMenu(String food, FoodType type) {
        logger.debug("addMenu : " + food);
        if (food.isEmpty()) {
            return addFail;
        }

        List<String> menu = selectMenuType(type);
        int idx = isExistingMenu(food, menu);
        if (idx != menu.size()) {
            return alreadyExistMenu;
        }

        menu.add(food);
        String msg = String.format(addFormat, food);
        logger.debug("DEBUG : {}", msg);

        MenuContextUtil.updateMenu(menu, type);

        return msg;
    }

    // TODO : Lunch List should be hash to remove in O(1)
    @Override
    public String removeMenu(String food, FoodType type) {
        logger.debug("remove Menu : " + food);
        if (food.isEmpty()) {
            return removeFail;
        }

        List<String> menu = selectMenuType(type);
        int idx = isExistingMenu(food, menu);

        if (idx == menu.size()) {
            return removeFail;
        }

        menu.remove(idx);
        String msg = String.format(removeFormat, food);
        logger.debug("DEBUG : {}", msg);

        MenuContextUtil.updateMenu(menu, type);

        return msg;
    }
}
