package net.mossol.bot.storage;

import java.util.List;
import java.util.Map;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.service.MenuServiceHandler.FoodType;

public interface MenuStorageService {

    Map<String, LocationInfo> getMenu(FoodType type);

    List<LocationInfo> getMenu();

    boolean removeMenu(FoodType foodType, String food);

    boolean removeMenu(FoodType foodType, LocationInfo locationInfo);

    boolean addMenu(FoodType foodType, String food);

    String addMenu(FoodType foodType, LocationInfo locationInfo);

    boolean updateMenu(String locationId, LocationInfo locationInfo);
}
