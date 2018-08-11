package net.mossol.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;
import net.mossol.model.SimpleText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class TextContextConfiguration extends ContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TextContextConfiguration.class);

    private static Map<String, SimpleText> convertToMenuInfo(JsonNode jsonNode) {
        try {
            List<SimpleText> info = OBJECT_MAPPER.readValue(OBJECT_MAPPER.treeAsTokens(jsonNode),
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
                TextContextConfiguration::convertToMenuInfo);
    }
}
