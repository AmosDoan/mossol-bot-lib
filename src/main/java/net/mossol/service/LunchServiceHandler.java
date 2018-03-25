package net.mossol.service;

import net.mossol.model.LocationInfo;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface LunchServiceHandler {

    enum FoodType {
        KOREA_FOOD,
        JAPAN_FOOD,
        DRINK_FOOD
    }

    String getMenu(FoodType type);
    String selectMenu(FoodType type);
    String getSelectedMenuFormat(String food);
    String addMenu(String food, FoodType type);
    String removeMenu(String food, FoodType type);
    LocationInfo getLocationInfo(String food);
}
