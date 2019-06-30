package net.mossol.bot.context;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.bot.model.LocationInfo;
import net.mossol.bot.util.MossolJsonUtil;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;

@Configuration
public class MenuContextConfiguration {
    private static final String CENTRAL_DOGMA_PROJECT = "mossol_menu";
    private static final String CENTRAL_DOGMA_REPOSITORY = "main";

    @Resource
    private CentralDogma centralDogma;

    @Bean
    public Watcher<Map<String, LocationInfo>> japanMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/japanMenu.json"),
                                        MossolJsonUtil::convertToMenuInfo);
    }

    @Bean
    public Watcher<Map<String, LocationInfo>> drinkMenuWatcher() {
        return centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                        Query.ofJsonPath("/drinkMenu.json"),
                                        MossolJsonUtil::convertToMenuInfo);
    }
}
