package net.mossol.bot.connection.Impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mossol.bot.connection.RetrofitClient;
import net.mossol.bot.model.LineReplyRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofit;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.annotation.Path;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

import retrofit2.Response;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConnectionImplTest {
    private static final Logger logger = LoggerFactory.getLogger(RetrofitConnectionImplTest.class);

    @RegisterExtension
    public static final ServerExtension server = new ServerExtension() {
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
    void test() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        final ClientFactory clientFactory = ClientFactory.insecure();
        final RetryRule retryRule = RetryRule.builder().onUnprocessed()
                                             .onServerErrorStatus().thenBackoff();

        final WebClient webClient = WebClient.of(server.httpsUri() + "/test/");
        final RetrofitClient retrofitClient =
                ArmeriaRetrofit.builder(webClient)
                               .factory(clientFactory)
                               .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                               .decorator(
                                       RetryingClient.builder(retryRule)
                                                     .responseTimeoutMillisForEachAttempt(5000)
                                                     .maxTotalAttempts(3)
                                                     .newDecorator()
                               )
                               .build()
                               .create(RetrofitClient.class);

        Response<Object> response = null;
        final LineReplyRequest request = new LineReplyRequest("aa");
        try {
            response = retrofitClient.sendReply("Bearer " + "aa", request).get();
        } catch (Exception e) {
            logger.info("Exception!! ", e);
        }

        logger.info("code<{}>body<{}>",  response.code(), response.body());

        assertThat(response.code()).isEqualTo(200);
    }
}
