package net.mossol.service.Impl;

import net.mossol.service.LunchServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private List<String> lunchCandidate = new ArrayList<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비",
            "버거킹", "숯불정식", "돈돈정", "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
            "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜"));
    private List<String> lunchJapanCandidate = new ArrayList<>(Arrays.asList("규카츠", "스시", "라멘", "돈카츠",
            "꼬치", "덴뿌라", "쉑쉑버거", "카레"));
    private Random random = new Random();

    private List<String> selectMenuType(foodType type) {
        if (type == foodType.KOREAN_FOOD) {
            logger.debug("KOREAN_FOOD");
            return lunchCandidate;
        } else if (type == foodType.JAPAN_FOOD) {
            logger.debug("JAPAN_FOOD");
            return lunchJapanCandidate;
        } else {
            return lunchCandidate;
        }
    }

    private int isExistinMenu(String food, List<String> menu) {
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
    public String getMenu(foodType type) {
        logger.debug("getMenu");
        List<String> menu = selectMenuType(type);
        String msg = String.format(menuFormat, String.join("\n", menu));
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String selectMenu(foodType type) {
        logger.debug("selectMenu");
        List<String> menu = selectMenuType(type);
        int idx = (random.nextInt() & Integer.MAX_VALUE)% menu.size();
        String select = menu.get(idx);
        String msg = String.format(selectFormat, select);
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String addMenu(String food, foodType type) {
        logger.debug("addMenu : " + food);
        List<String> menu = selectMenuType(type);
        int idx = isExistinMenu(food, menu);
        if (idx != menu.size()) {
            return alreadyExistMenu;
        }

        menu.add(food);
        String msg = String.format(addFormat, food);
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    // TODO : Lunch List should be hash to remove in O(1)
    @Override
    public String removeMenu(String food, foodType type) {
        logger.debug("remove Menu : " + food);
        List<String> menu = selectMenuType(type);
        int idx = isExistinMenu(food, menu);

        if (idx == menu.size()) {
            return removeFail;
        }

        menu.remove(idx);
        String msg = String.format(removeFormat, food);
        logger.debug("DEBUG : {}", msg);
        return msg;
    }
}
