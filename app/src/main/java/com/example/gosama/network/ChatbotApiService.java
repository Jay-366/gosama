package com.example.gosama.network;

import com.example.gosama.model.ChatRequest;
import com.example.gosama.model.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatbotApiService {
    
    @POST("chat")
    Call<ChatResponse> sendChatMessage(@Body ChatRequest request);
    
    @POST("ride-assistance")
    Call<ChatResponse> sendRideAssistanceMessage(@Body ChatRequest request);
} 