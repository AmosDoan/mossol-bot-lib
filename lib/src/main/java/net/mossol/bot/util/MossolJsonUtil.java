package net.mossol.bot.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.mossol.bot.model.LineRequest;
import net.mossol.bot.model.LocationInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public final class MossolJsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(MossolJsonUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Map<String, LocationInfo> convertToMenuInfo(JsonNode jsonNode) {
        try {
            List<LocationInfo> info = OBJECT_MAPPER.readValue(OBJECT_MAPPER.treeAsTokens(jsonNode),
                                                              new TypeReference<List<LocationInfo>>(){});
            return info.stream().collect(Collectors.toMap(LocationInfo::getTitle, Function.identity()));
        } catch (IOException e) {
            logger.error("Converting Json to LocationInfo Map Failed", e);
            return null;
        }
    }

    public static LineRequest readJsonString(JsonNode jsonNode) {
        try {
            LineRequest request;
            request = OBJECT_MAPPER.treeToValue(jsonNode, LineRequest.class);
            return request;
        } catch (Exception e) {
            logger.debug("[ERROR] Converting to object failed. Received Json String <{}>  cause : <{}>",
                         jsonNode,
                         e.getMessage());
            return null;
        }
    }

    public static String writeJsonString(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
