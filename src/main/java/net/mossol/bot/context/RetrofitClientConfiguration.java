package net.mossol.bot.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;
import com.linecorp.armeria.client.retry.RetryStrategy;
import com.linecorp.armeria.client.retry.RetryingHttpClientBuilder;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.mossol.bot.connection.RetrofitClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class RetrofitClientConfiguration {

    @Value("${retrofit.baseUrl}")
    private String baseUrl;

    @Value("${retrofit.connectionTimeOutMills}")
    private int httpSocketTimeOutMills;

    @Value("${retrofit.maxRetry}")
    private int maxRetry;

    @Bean
    public RetrofitClient retrofitClient() {
        final ObjectMapper objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        ClientFactory clientFactory = new ClientFactoryBuilder()
                .sslContextCustomizer(b -> b.trustManager(InsecureTrustManagerFactory.INSTANCE))
                .build();

        RetrofitClient retrofitClient = new ArmeriaRetrofitBuilder(clientFactory)
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .withClientOptions((url, option) -> {
                    option.decorator(HttpRequest.class, HttpResponse.class,
                            new RetryingHttpClientBuilder(RetryStrategy.onServerErrorStatus())
                                    .responseTimeoutMillisForEachAttempt(httpSocketTimeOutMills)
                                    .maxTotalAttempts(maxRetry)
                                    .newDecorator());
                    return option;
                })
                .build()
                .create(RetrofitClient.class);
        return retrofitClient;
    }
}
