package net.mossol.bot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.linecorp.centraldogma.client.CentralDogma;

/**
 * Created by Amos.Doan.Mac on 2017. 11. 18..
 */
@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "net.mossol"})
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(CentralDogma dogma) {
        return args -> {
            System.err.println(dogma.listProjects().join());
        };
    }
}
