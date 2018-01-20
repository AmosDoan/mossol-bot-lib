package net.mossol.context;

import java.io.IOException;
import java.util.Set;

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

@Configuration
public class MenuContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MenuContextConfiguration.class);
    private static final CentralDogma centralDogma = CentralDogma.forHost("mossol.net");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CENTRAL_DOGMA_PROJECT = "Mossol_Line_BOT";
    private static final String CENTRAL_DOGMA_REPOSITORY = "main";

    private static Set<String> convertToSet(JsonNode jsonNode) {
        try {
            return OBJECT_MAPPER.readValue(OBJECT_MAPPER.treeAsTokens(jsonNode),
                    new TypeReference<Set<String>>(){});
        } catch (IOException e) {
            logger.error("Converting Json to List Failed");
            return null;
        }
    }

    @Bean
    public Watcher<Set<String>> japanMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                Query.ofJsonPath("/japanMenu.json"),
                MenuContextConfiguration::convertToSet);
    }

    @Bean
     public Watcher<Set<String>> koreaMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                Query.ofJsonPath("/koreaMenu.json"),
                MenuContextConfiguration::convertToSet);
    }
}
