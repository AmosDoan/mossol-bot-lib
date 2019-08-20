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
import net.mossol.bot.model.MenuType;
import net.mossol.bot.service.MenuServiceHandler;
import net.mossol.bot.storage.MenuStorageService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Slf4j
@Service
public class MenuServiceHandlerImpl implements MenuServiceHandler {
    private static final String MENU_FORMAT = "메뉴 리스트는 다음과 같아요 멍\n%s";
    private static final String MENU_ADDED_FORMAT = "멍멍 %s 추가합니다";
    private static final String MENU_REMOVED_FORMAT = "멍멍 %s 가지마세요! 가면 깨뭅니다";
    private static final String FAIL_REMOVE_MENU = "멍멍 그런 메뉴 없어요";
    private static final String ALREADY_EXIST_MENU = "멍멍 이미 있는 곳이에요";
    private static final String FAIL_ADD_MENU = "왈왈! 추가 실패!!";

    private final Random random = new Random();

    @Resource
    private MenuStorageService menuStorageService;

    @Override
    public String getMenu(MenuType type) {
        Set<String> menu = menuStorageService.getMenuList(type)
                                             .entrySet().stream().map(e -> e.getValue().getTitle())
                                             .collect(Collectors.toSet());
        String msg = String.format(MENU_FORMAT, String.join("\n", menu));
        log.debug("DEBUG : {}", msg);
        return msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocationInfo selectMenu(MenuType type) {
        log.debug("selectMenu");
        Map<String, LocationInfo> menu = menuStorageService.getMenuList(type);
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
    public String addMenu(List<String> foods, MenuType type) {
        final String food = foods.get(0);

        log.debug("addMenu : " + food);
        if (food.isEmpty()) {
            return FAIL_ADD_MENU;
        }

        if (!menuStorageService.addMenu(type, food)) {
            return ALREADY_EXIST_MENU;
        }

        String msg = String.format(MENU_ADDED_FORMAT, food);
        log.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String removeMenu(List<String> menus, MenuType type) {
        final String food = menus.get(0);

        log.debug("remove Menu : " + food);
        if (food.isEmpty()) {
            return FAIL_REMOVE_MENU;
        }

        if (!menuStorageService.removeMenu(type, food)) {
            return FAIL_REMOVE_MENU;
        }

        String msg = String.format(MENU_REMOVED_FORMAT, food);
        log.debug("DEBUG : {}", msg);
        return msg;
    }
}
