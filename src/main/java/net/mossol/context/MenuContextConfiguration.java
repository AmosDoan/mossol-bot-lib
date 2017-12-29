package net.mossol.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.common.Query;
import com.linecorp.centraldogma.common.Revision;
import net.mossol.service.LunchServiceHandler.FoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class MenuContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MenuContextConfiguration.class);
    private CentralDogma centralDogma = CentralDogma.forHost("mossol.net");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final List<String> japanDefaultCandidate =
            new ArrayList<>(Arrays.asList("규카츠", "스시", "라멘", "돈카츠", "꼬치", "덴뿌라", "쉑쉑버거", "카레"));
    private static final List<String> koreaDefaultCandidate =
            new ArrayList<>(Arrays.asList("부대찌개", "청담소반", "설렁탕", "카레", "닭갈비", "버거킹", "숯불정식", "돈돈정",
                    "브라운돈까스", "차슈멘연구소", "유타로", "짬뽕", "쉑쉑버거", "하야시라이스", "보쌈", "하치돈부리",
                    "홍대개미", "B사감", "콩나물국밥", "순대국밥", "김치찜", "화수목"));

    private List<String> convertToList(JsonNode jsonNode, FoodType foodType) {
        try {
            return OBJECT_MAPPER.readValue(OBJECT_MAPPER.treeAsTokens(jsonNode),
                    new TypeReference<List<String>>(){});
        } catch (IOException e) {
            logger.error("Converting Json to List Failed");
            if (foodType == FoodType.JAPAN_FOOD) {
                return japanDefaultCandidate;
            } else {
                return koreaDefaultCandidate;
            }
        }
    }

    @Bean
    public List<String> japanMenu() {
        try {
            JsonNode menu =
                    centralDogma.getFile("Mossol_Line_BOT", "main", Revision.HEAD,
                            Query.ofJsonPath("/japanMenu.json"))
                            .get(10, TimeUnit.SECONDS).content();
            List<String> menuList = convertToList(menu, FoodType.JAPAN_FOOD);
            logger.debug("Getting Japan Menu from CD Success : {}", menuList);
            return menuList;
        } catch (Exception e) {
            logger.error("Getting Japan Menu Failed");
            return japanDefaultCandidate;
        }
    }

    @Bean
    public List<String> koreaMenu() {
        try {
            JsonNode menu =
                    centralDogma.getFile("Mossol_Line_BOT", "main", Revision.HEAD,
                            Query.ofJsonPath("/koreaMenu.json"))
                            .get(10, TimeUnit.SECONDS).content();
            List<String> menuList = convertToList(menu, FoodType.KOREA_FOOD);
            logger.debug("Getting Korean Menu from CD Success : {}", menuList);
            return menuList;
        } catch (Exception e) {
            logger.error("Getting Korean Menu Failed");
            return koreaDefaultCandidate;
        }
    }
}
