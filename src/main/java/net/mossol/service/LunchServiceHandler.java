package net.mossol.service;

import net.mossol.service.Impl.LunchServiceHandlerImpl;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface LunchServiceHandler {

    enum foodType {
        KOREAN_FOOD,
        JAPAN_FOOD,
    };

    String getMenu(foodType type);
    String selectMenu(foodType type);
    String addMenu(String food, foodType type);
    String removeMenu(String food, foodType type);
}
