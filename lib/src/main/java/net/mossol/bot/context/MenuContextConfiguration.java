package net.mossol.bot.context;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.mossol.bot.model.LocationInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;

import javax.annotation.Resource;

@Configuration
public class MenuContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MenuContextConfiguration.class);
    private static final String CENTRAL_DOGMA_PROJECT = "mossol_menu";
    private static final String CENTRAL_DOGMA_REPOSITORY = "main";

    @Resource
    private CentralDogma centralDogma;

    @Resource
    private ObjectMapper objectMapper;

    private Map<String, LocationInfo> convertToMenuInfo(JsonNode jsonNode) {
        try {
            List<LocationInfo> info = objectMapper.readValue(objectMapper.treeAsTokens(jsonNode),
                                                             new TypeReference<List<LocationInfo>>(){});
            return info.stream().collect(Collectors.toMap(LocationInfo::getTitle, Function.identity()));
        } catch (IOException e) {
            logger.error("Converting Json to LocationInfo Map Failed", e);
            return null;
        }
    }

    @Bean
    public Watcher<Map<String, LocationInfo>> japanMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/japanMenu.json"),
                                        this::convertToMenuInfo);
    }

    @Bean
    public Watcher<Map<String, LocationInfo>> koreaMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/koreaMenu.json"),
                                        this::convertToMenuInfo);
    }

    @Bean
    public Watcher<Map<String, LocationInfo>> drinkMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/drinkMenu.json"),
                                        this::convertToMenuInfo);
    }
}
