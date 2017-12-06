package net.mossol.service.Impl;

import net.mossol.service.LunchServiceHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class LunchServiceHandlerImpl implements LunchServiceHandler {

    private final String menuFormat = "메뉴 리스트는 다음과 같아요 멍 \n %s";
    private final String selectFormat = "멍멍 %s 안먹으면 가서 깨뭅니다";
    private List<String> lunchCandidate = new ArrayList<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비"));
    private Random random = new Random();

    @Override
    public String getMenu() {
        System.out.println("getMenu");
        String msg = String.format(menuFormat, String.join("\n", lunchCandidate));
        System.out.println("DEBUG : " + msg);
        return msg;
    }

    @Override
    public String selectMenu() {
        System.out.println("selectMenu");
        int idx = random.nextInt() % lunchCandidate.size() - 1;
        String select = lunchCandidate.get(idx);
        String msg = String.format(selectFormat, select);
        System.out.println("DEBUG : " + msg);
        return msg;
    }
}
