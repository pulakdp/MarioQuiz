package io.github.pulakdp.marioquiz.data;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Author: PulakDebasish
 */
public interface QuestionsApiInterface {
    @GET("/api/v1/game")
    Call<QuestionsResponse> getQuestions();
}
