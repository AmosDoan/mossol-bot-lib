package net.mossol.context;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.model.MenuInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;

@Configuration
public class MenuContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MenuContextConfiguration.class);
    private static final CentralDogma centralDogma = CentralDogma.forHost("mossol.net");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CENTRAL_DOGMA_PROJECT = "mossol";
    private static final String CENTRAL_DOGMA_REPOSITORY = "main";

    private static Map<String, MenuInfo> convertToMenuInfo(JsonNode jsonNode) {
        try {
            List<MenuInfo> info = OBJECT_MAPPER.readValue(OBJECT_MAPPER.treeAsTokens(jsonNode),
                                                          new TypeReference<Collection<MenuInfo>>(){});
            return info.stream().collect(Collectors.toMap(MenuInfo::getTitle, Function.identity()));
        } catch (IOException e) {
            logger.error("Converting Json to MenuInfo Map Failed", e);
            return null;
        }
    }

    @Bean
    public Watcher<Map<String, MenuInfo>> japanMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/japanMenu.json"),
                                        MenuContextConfiguration::convertToMenuInfo);
    }

    @Bean
    public Watcher<Map<String, MenuInfo>> koreaMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/koreaMenu.json"),
                                        MenuContextConfiguration::convertToMenuInfo);
    }

    @Bean
    public Watcher<Map<String, MenuInfo>> drinkMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/drinkMenu.json"),
                                        MenuContextConfiguration::convertToMenuInfo);
    }
}
