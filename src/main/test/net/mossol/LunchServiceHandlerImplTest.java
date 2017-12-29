package net.mossol;

import net.mossol.service.Impl.LunchServiceHandlerImpl;
import net.mossol.service.LunchServiceHandler;
import org.junit.Test;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public class LunchServiceHandlerImplTest {

    private LunchServiceHandler lunchServiceHandler = new LunchServiceHandlerImpl();

    @Test
    public void selectMenu() {
        String menu = lunchServiceHandler.selectMenu(LunchServiceHandler.FoodType.KOREA_FOOD);
        System.out.println("msg : " + menu);

        menu = lunchServiceHandler.selectMenu(LunchServiceHandler.FoodType.JAPAN_FOOD);
        System.out.println("msg : " + menu);
    }
}
