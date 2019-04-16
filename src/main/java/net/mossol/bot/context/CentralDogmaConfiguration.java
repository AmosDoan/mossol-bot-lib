package net.mossol.bot.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.centraldogma.client.CentralDogma;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
class CentralDogmaConfiguration {
    @Value("${centraldogma.host}")
    private String host;

    @Value("${centraldogma.port}")
    private int port;

    @Bean
    public CentralDogma centralDogma() {
        return CentralDogma.forHost(host, port);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
