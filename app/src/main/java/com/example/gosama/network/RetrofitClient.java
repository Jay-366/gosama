package com.example.gosama.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    // Use "http://your-server-ip:3000/" for real device
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ChatbotApiService getApi() {
        return retrofit.create(ChatbotApiService.class);
    }

    public static void setBaseUrl(String baseUrl) {
        // This method can be used to change the base URL at runtime
        // For example, when switching between development and production servers
        instance = null; // Force recreation with new URL
    }
} 