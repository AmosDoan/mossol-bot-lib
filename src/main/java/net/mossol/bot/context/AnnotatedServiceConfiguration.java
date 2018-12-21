package net.mossol.bot.context;

import javax.annotation.Resource;

import net.mossol.bot.MossolMessageController;
import net.mossol.bot.MossolLineController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class AnnotatedServiceConfiguration {

    @Resource
    MossolLineController mossolLineController;

    @Resource
    MossolMessageController mossolMessageController;

    @Bean
    public AnnotatedServiceRegistrationBean mossolHandler() {
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
