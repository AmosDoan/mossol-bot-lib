package net.mossol.bot.service;

import net.mossol.bot.model.LocationInfo;

import java.util.List;

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
    LocationInfo selectMenu(FoodType type);
    String addMenu(List<String> food, FoodType type);
    String removeMenu(List<String> food, FoodType type);
}
