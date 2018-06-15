package net.mossol.context;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.MossolController;

import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class AnnotatedServiceConfiguration {

    @Resource
    MossolController mossolController;

    @Bean
    public AnnotatedServiceRegistrationBean mossolHandler() {
        return new AnnotatedServiceRegistrationBean().setServiceName("MOSSOL")
                                                     .setPathPrefix("/")
                                                     .setService(mossolController);
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator() {
        return serverBuilder ->
            serverBuilder.accessLogFormat("%h %l %u %t '%r' %s %b '%{X-Forwarded-For}i' '%{Referer}i'"
                                          + " '%{User-Agent}i' '%{Cookie}i'");
    }
}
