package net.mossol.context;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mossol.model.MenuInfo;
import net.mossol.service.MenuServiceHandler.FoodType;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.common.Author;
import com.linecorp.centraldogma.common.Change;
import com.linecorp.centraldogma.common.Commit;
import com.linecorp.centraldogma.common.Revision;
import com.linecorp.centraldogma.internal.thrift.CentralDogmaException;

public final class MenuContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(MenuContextConfiguration.class);
    private static final CentralDogma centralDogma = CentralDogma.forHost("mossol.net");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MenuContextUtil(){}

    private static String convertToJsonNode(Map<String, MenuInfo> map) {
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (IOException e) {
            logger.error("Converting Json to Map Failed");
            return null;
        }
    }

    public static void updateMenu(Map<String, MenuInfo> menu, FoodType foodType) {
        String jsonMenu = convertToJsonNode(menu);
        if (jsonMenu == null) {
            return;
        }

        String jsonPath;
        if (foodType == FoodType.JAPAN_FOOD) {
            jsonPath = "/japanMenu.json";
        } else {
            jsonPath = "/koreaMenu.json";
        }

        CompletableFuture<Commit> future = null;
        try {
             future =
                    centralDogma.push("mossol", "main", Revision.HEAD,
                            new Author("Mossol", "amos.doan@gmail.com"),
                            "Add new Menu",
                            Change.ofJsonUpsert(jsonPath, jsonMenu));
        } catch (Exception e) {
            logger.debug("Menu Update Failed : {} {} ", jsonPath, e);
        }

        try {
            future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CentralDogmaException) {
                CentralDogmaException cde = (CentralDogmaException) cause;
                logger.debug("Menu Update Failed : {} {} ", cde.getErrorCode(), cde.getMessage());
            }
        }
    }
}
