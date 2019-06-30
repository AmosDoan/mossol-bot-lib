package net.mossol.bot.connection.Impl;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mossol.bot.connection.RetrofitClient;
import net.mossol.bot.model.LineReplyRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;
import com.linecorp.armeria.client.retry.RetryStrategy;
import com.linecorp.armeria.client.retry.RetryingHttpClientBuilder;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.annotation.Path;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.server.ServerRule;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import retrofit2.Response;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConnectionImplTest {
    private final static Logger logger = LoggerFactory.getLogger(RetrofitConnectionImplTest.class);

    @ClassRule
    public static final ServerRule rule = new ServerRule() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.annotatedService("/test", new MyAnnotatedService(), LoggingService.newDecorator()).tlsSelfSigned();
        }
    };

    public static class MyAnnotatedService {
        @Post
        @Path("/v2/bot/message/reply")
        public String reply() {
            logger.info("reply!!");
            return "{\"test\" : 1}";
        }
    }

    @Test
    public void test() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        ClientFactory clientFactory = new ClientFactoryBuilder()
                .sslContextCustomizer(b -> b.trustManager(InsecureTrustManagerFactory.INSTANCE))
                .build();

        RetrofitClient retrofitClient = new ArmeriaRetrofitBuilder(clientFactory)
                .baseUrl(rule.httpsUri("/test/"))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .withClientOptions((url, option) -> {
                    option.decorator(
                            new RetryingHttpClientBuilder(RetryStrategy.onServerErrorStatus())
                                    .responseTimeoutMillisForEachAttempt(5000)
                                    .maxTotalAttempts(3)
                                    .newDecorator());
                    return option;
                })
                .build()
                .create(RetrofitClient.class);

        Response<Object> response = null;
        LineReplyRequest request = new LineReplyRequest("aa");
        try {
            response = retrofitClient.sendReply("Bearer " + "aa", request).get();
        } catch (Exception e) {
            logger.info("Exception!! ", e);
        }

        logger.info("code<{}>body<{}>",  response);

    }
}
