package net.mossol.bot.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.service.MenuServiceHandler;
import net.mossol.bot.storage.MenuStorageService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Slf4j
@Service
public class MenuServiceHandlerImpl implements MenuServiceHandler {
    private static final String menuFormat = "메뉴 리스트는 다음과 같아요 멍\n%s";

    private static final String addFormat = "멍멍 %s 추가합니다";
    private static final String removeFormat = "멍멍 %s 가지마세요! 가면 깨뭅니다";
    private static final String removeFail = "멍멍 그런 메뉴 없어요";
    private static final String alreadyExistMenu = "멍멍 이미 있는 곳이에요";
    private static final String addFail = "왈왈! 추가 실패!!";

    private final Random random = new Random();

    @Resource
    private MenuStorageService menuStorageService;

    @Override
    public String getMenu(FoodType type) {
        log.debug("getMenu");
        Set<String> menu = menuStorageService.getMenu(type)
                                             .entrySet().stream().map(e -> e.getValue().getTitle())
                                             .collect(Collectors.toSet());
        String msg = String.format(menuFormat, String.join("\n", menu));
        log.debug("DEBUG : {}", msg);
        return msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocationInfo selectMenu(FoodType type) {
        log.debug("selectMenu");
        Map<String, LocationInfo> menu = menuStorageService.getMenu(type);
        int idx = (random.nextInt() & Integer.MAX_VALUE) % menu.size();

        log.debug("idx: {} Selected Menu : {}", idx, menu.keySet());
        Iterator<String> iterator = menu.keySet().iterator();
        for (int i = 0; i < idx; i++) {
            iterator.next();
        }

        String select = iterator.next();
        log.debug("Selected Menu : {}", select);
        return menu.get(select);
    }

    @Override
    public String addMenu(List<String> foods, FoodType type) {
        final String food = foods.get(0);

        log.debug("addMenu : " + food);
        if (food.isEmpty()) {
            return addFail;
        }

        if (!menuStorageService.addMenu(type, food)) {
            return alreadyExistMenu;
        }

        String msg = String.format(addFormat, food);
        log.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String removeMenu(List<String> foods, FoodType type) {
        final String food = foods.get(0);

        log.debug("remove Menu : " + food);
        if (food.isEmpty()) {
            return removeFail;
        }

        if (!menuStorageService.removeMenu(type, food)) {
            return removeFail;
        }

        String msg = String.format(removeFormat, food);
        log.debug("DEBUG : {}", msg);
        return msg;
    }
}
