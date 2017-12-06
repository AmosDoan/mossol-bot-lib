package net.mossol;

import net.mossol.service.Impl.LunchServiceHandlerImpl;
import net.mossol.service.LunchServiceHandler;
import org.junit.Test;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public class LunchServiceHandlerImplTest {

    @Test
    public void selectMenu() {
        LunchServiceHandler lunchServiceHandler = new LunchServiceHandlerImpl();
        String menu = lunchServiceHandler.selectMenu();
        System.out.println("msg : " + menu);
    }
}
