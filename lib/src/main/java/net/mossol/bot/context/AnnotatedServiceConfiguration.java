package net.mossol.bot.context;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.bot.controller.MossolLineController;
import net.mossol.bot.controller.MossolMessageController;

import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class AnnotatedServiceConfiguration {

    @Resource
    private MossolLineController mossolLineController;

    @Resource
    private MossolMessageController mossolMessageController;

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(
            @Value("${service.enabled.line}") boolean enableLine) {
        return sb -> {
            sb.accessLogFormat("%h %l %u %t '%r' %s %b '%{X-Forwarded-For}i' '%{Referer}i'"
                               + " '%{User-Agent}i' '%{Cookie}i'");
            sb.annotatedService(mossolMessageController);
            if (enableLine) {
                sb.annotatedService(mossolLineController);
            }
            sb.decorator(
                    CorsService.builderForAnyOrigin()
                               .allowRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT,
                                                    HttpMethod.DELETE)
                               .newDecorator());
        };
    }
}
