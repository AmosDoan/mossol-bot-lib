package net.mossol.service;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface LunchServiceHandler {

    enum FoodType {
        KOREA_FOOD,
        JAPAN_FOOD,
    }

    String getMenu(FoodType type);
    String selectMenu(FoodType type);
    String addMenu(String food, FoodType type);
    String removeMenu(String food, FoodType type);
}
