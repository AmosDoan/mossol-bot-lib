package net.mossol.bot.service;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.model.MenuType;

import java.util.List;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface MenuServiceHandler {

    String getMenu(MenuType type);
    LocationInfo selectMenu(MenuType type);
    String addMenu(List<String> food, MenuType type);
    String removeMenu(List<String> food, MenuType type);
}
