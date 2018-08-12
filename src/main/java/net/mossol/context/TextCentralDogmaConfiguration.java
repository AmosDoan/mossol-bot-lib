package net.mossol.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;
import net.mossol.model.SimpleText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class TextCentralDogmaConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TextCentralDogmaConfiguration.class);
    private static final String CENTRAL_DOGMA_PROJECT = "mossol";
    private static final String CENTRAL_DOGMA_REPOSITORY = "main";

    @Resource
    private CentralDogma centralDogma;

    @Resource
    private ObjectMapper objectMapper;


    private Map<String, SimpleText> convertToMenuInfo(JsonNode jsonNode) {
        try {
            List<SimpleText> info = objectMapper.readValue(objectMapper.treeAsTokens(jsonNode),
                    new TypeReference<List<SimpleText>>(){});
            return info.stream().collect(Collectors.toMap(SimpleText::getMessage, Function.identity()));
        } catch (IOException e) {
            logger.error("Converting Json to SimpleText Map Failed", e);
            return null;
        }
    }

    @Bean
    public Watcher<Map<String, SimpleText>> simpleTextWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                Query.ofJsonPath("/simpleText.json"),
                this::convertToMenuInfo);
    }
}
