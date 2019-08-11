package net.mossol.bot.storage;

import java.util.List;
import java.util.Map;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.model.MenuType;

public interface MenuStorageService {

    Map<String, LocationInfo> getMenuList(MenuType type);

    List<LocationInfo> getAllLocationInfoList();

    boolean removeMenu(MenuType menuType, String food);

    boolean removeLocationInfo(LocationInfo locationInfo);

    boolean addMenu(MenuType menuType, String food);

    String addLocationInfo(LocationInfo locationInfo);

    boolean updateLocationInfo(String locationId, LocationInfo locationInfo);
}
