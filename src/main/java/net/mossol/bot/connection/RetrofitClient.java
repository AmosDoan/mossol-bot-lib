package net.mossol.bot.connection;

import net.mossol.bot.model.LineReplyRequest;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.concurrent.CompletableFuture;

public interface RetrofitClient {
    String CONTENT_TYPE_JSON_UTF8 = "content-type: application/json; charset=UTF-8";

    @POST("v2/bot/message/reply")
    @Headers(CONTENT_TYPE_JSON_UTF8)
    CompletableFuture<Response<Object>> sendReply(@Header("Authorization") String token,
                                                  @Body LineReplyRequest request);
    @POST("/v2/bot/group/{groupId}/leave")
    @Headers(CONTENT_TYPE_JSON_UTF8)
    CompletableFuture<Response<Object>> leaveRoom(@Path("groupId") String groupId,
                                                  @Header("Authorization") String token,
                                                  @Body Object dummy);
}
