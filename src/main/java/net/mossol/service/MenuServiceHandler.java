package net.mossol.service;

import net.mossol.model.MenuInfo;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface MenuServiceHandler {

    enum FoodType {
        KOREA_FOOD,
        JAPAN_FOOD,
        DRINK_FOOD
    }

    String getMenu(FoodType type);
    MenuInfo selectMenu(FoodType type);
    String addMenu(String food, FoodType type);
    String removeMenu(String food, FoodType type);
}
