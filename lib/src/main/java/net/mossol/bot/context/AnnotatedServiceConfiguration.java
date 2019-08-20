package net.mossol.bot.context;

import javax.annotation.Resource;

import net.mossol.bot.controller.HealthCheckController;
import net.mossol.bot.controller.MossolMessageController;
import net.mossol.bot.controller.MossolLineController;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class AnnotatedServiceConfiguration {

    @Resource
    HealthCheckController healthCheckController;

    @Resource
    MossolLineController mossolLineController;

    @Resource
    MossolMessageController mossolMessageController;

    @Bean
    public AnnotatedServiceRegistrationBean healthCheckHandler() {
        return new AnnotatedServiceRegistrationBean().setServiceName("HEALTH_CHECK")
                                                     .setPathPrefix("/")
                                                     .setService(healthCheckController);
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.enabled.line",
            havingValue = "true",
            matchIfMissing = true
    )
    public AnnotatedServiceRegistrationBean mossolLineHandler() {
        return new AnnotatedServiceRegistrationBean().setServiceName("MOSSOL")
                                                     .setPathPrefix("/")
                                                     .setService(mossolLineController);
    }

    @Bean
    public AnnotatedServiceRegistrationBean mossolMessageHandler() {
        return new AnnotatedServiceRegistrationBean().setServiceName("MOSSOL_MSG")
                                                     .setPathPrefix("/")
                                                     .setService(mossolMessageController);
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator() {
        return serverBuilder ->
            serverBuilder.accessLogFormat("%h %l %u %t '%r' %s %b '%{X-Forwarded-For}i' '%{Referer}i'"
                                          + " '%{User-Agent}i' '%{Cookie}i'");
    }
}
