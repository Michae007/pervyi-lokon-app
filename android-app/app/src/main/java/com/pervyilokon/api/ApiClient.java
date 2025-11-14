package com.pervyilokon.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // ⚠️ ЗАМЕНИТЕ НА ВАШ URL БЕЗ /exec В КОНЦЕ!
    private static final String BASE_URL = "https://script.google.com/macros/s/AKfycbyKfiewtGjIXo8KdzCasp0haJYwTZD9OtA4CqQwK8aYhCZRxJ0pG0p6ytUNij-sI3Rx4w/";
    private static Retrofit retrofit = null;

    public static GoogleSheetsApi getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GoogleSheetsApi.class);
    }
}
