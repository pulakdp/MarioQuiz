package io.github.pulakdp.marioquiz.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author: PulakDebasish
 */
public class QuestionsApiClient {

    public static final String BASE_URL = "http://5b1bd9a16e0fd400146aaf86.mockapi.io";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null)
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        return retrofit;
    }

}
