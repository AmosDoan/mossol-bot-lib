package net.mossol.context;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.MossolController;

import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;

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
}
