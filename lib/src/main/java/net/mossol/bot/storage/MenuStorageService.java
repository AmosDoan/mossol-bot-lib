package net.mossol.bot.storage;

import java.util.List;
import java.util.Map;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.model.MenuType;

public interface MenuStorageService {

    Map<String, LocationInfo> getMenu(MenuType type);

    List<LocationInfo> getMenu();

    boolean removeMenu(MenuType menuType, String food);

    boolean removeMenu(MenuType menuType, LocationInfo locationInfo);

    boolean addMenu(MenuType menuType, String food);

    String addMenu(MenuType menuType, LocationInfo locationInfo);

    boolean updateMenu(String locationId, LocationInfo locationInfo);
}
