package net.mossol.bot.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.bot.connection.RetrofitClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofit;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;

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

        final ClientFactory clientFactory = ClientFactory.insecure();
        final RetryRule retryRule = RetryRule.builder().onUnprocessed()
                                             .onServerErrorStatus().thenBackoff();

        return ArmeriaRetrofit.builder(baseUrl)
                              .factory(clientFactory)
                              .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                              .decorator(
                                      RetryingClient.builder(retryRule)
                                      .responseTimeoutMillisForEachAttempt(httpSocketTimeOutMills)
                                      .maxTotalAttempts(maxRetry)
                                      .newDecorator()
                              )
                              .build()
                .create(RetrofitClient.class);
    }
}
