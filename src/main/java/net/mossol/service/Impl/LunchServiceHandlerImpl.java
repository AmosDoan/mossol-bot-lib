package net.mossol.service.Impl;

import net.mossol.service.LunchServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger logger = LoggerFactory.getLogger(LunchServiceHandlerImpl.class);
    private final String menuFormat = "메뉴 리스트는 다음과 같아요 멍\n%s";
    private final String selectFormat = "멍멍 %s 안먹으면 가서 깨뭅니다";
    private List<String> lunchCandidate = new ArrayList<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비",
            "버거킹", "숯불정식", "돈돈정", "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
            "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜"));
    private Random random = new Random();

    @Override
    public String getMenu() {
        logger.debug("getMenu");
        String msg = String.format(menuFormat, String.join("\n", lunchCandidate));
        logger.debug("DEBUG : {}", msg);
        return msg;
    }

    @Override
    public String selectMenu() {
        logger.debug("selectMenu");
        int idx = (random.nextInt() & Integer.MAX_VALUE)% lunchCandidate.size();
        String select = lunchCandidate.get(idx);
        String msg = String.format(selectFormat, select);
        logger.debug("DEBUG : {}", msg);
        return msg;
    }
}
