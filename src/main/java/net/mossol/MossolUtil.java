package net.mossol;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mossol.model.LineRequest;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public final class MossolUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static LineRequest readJsonString(String jsonString) {
        try {
            LineRequest request;
            request = OBJECT_MAPPER.readValue(jsonString, LineRequest.class);
            return request;
        } catch (Exception e) {
            System.out.println("[ERROR] Converting to object failed");
            return null;
        }
    }

    public static String writeJsonString(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return json;
        } catch (Exception e) {
            return null;
        }
    }


}
