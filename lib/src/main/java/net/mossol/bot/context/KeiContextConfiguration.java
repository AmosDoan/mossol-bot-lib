package net.mossol.bot.context;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

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
public class KeiContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(KeiContextConfiguration.class);
    private static final String CENTRAL_DOGMA_PROJECT = "mossol";
    private static final String CENTRAL_DOGMA_REPOSITORY = "main";

    @Resource
    private CentralDogma centralDogma;

    @Resource
    private ObjectMapper objectMapper;

    private List<String> convertToList(JsonNode jsonNode) {
        try {
            List<String> info = objectMapper.readValue(objectMapper.treeAsTokens(jsonNode),
                                                       new TypeReference<List<String>>(){});
            return info;
        } catch (IOException e) {
            logger.error("Converting Json to LocationInfo Map Failed", e);
            return null;
        }
    }

    @Bean
    public Watcher<List<String>> keiUnitWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/kei_unit.json", "$.members"),
                                        this::convertToList);
    }

}
