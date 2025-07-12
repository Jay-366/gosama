package com.example.gosama;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gosama.adapter.ChatAdapter;
import com.example.gosama.model.ChatMessage;
import com.example.gosama.model.ChatRequest;
import com.example.gosama.model.ChatResponse;
import com.example.gosama.network.ChatbotApiService;
import com.example.gosama.network.RetrofitClient;
import com.example.gosama.viewmodel.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.UUID;

public class AssistantChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private ChatbotApiService apiService;
    private UserViewModel userViewModel;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_chat);
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewAssistantMessages);
        editTextMessage = findViewById(R.id.editTextAssistantMessage);
        buttonSend = findViewById(R.id.buttonAssistantSend);
        
        // Initialize UserViewModel
        userViewModel = new UserViewModel();
        
        // Observe user data and get current user ID
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUserId = user.getUid(); // CORRECT: uses UID
            } else {
                currentUserId = null;
            }
        });
        
        // Load user data
        userViewModel.loadUserData();
        
        // Set default user ID initially
        currentUserId = "anonymous";
        
        // Initialize API service
        apiService = RetrofitClient.getInstance().getApi();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Add welcome message
        addWelcomeMessage();
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }
    
    private void setupClickListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }
    
    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
            "Hello! I'm your GoSama ride assistant. I can help you with:\n" +
            "• Booking rides\n" +
            "• Finding drivers\n" +
            "• Navigation assistance\n" +
            "• Ride history\n" +
            "• Payment questions\n\n" +
            "How can I help you today?",
            false
        );
        welcomeMessage.setType("assistant");
        chatAdapter.addMessage(welcomeMessage);
    }
    
    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Clear input
        editTextMessage.setText("");
        
        // Add user message to chat
        ChatMessage userMessage = new ChatMessage(message, true);
        userMessage.setUserId(currentUserId);
        userMessage.setType("user");
        chatAdapter.addMessage(userMessage);
        
        // Scroll to bottom
        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        
        // Show loading message
        ChatMessage loadingMessage = new ChatMessage("Typing...", false);
        loadingMessage.setType("assistant");
        chatAdapter.addMessage(loadingMessage);
        
        // Send to API
        sendToChatbot(message, loadingMessage);
    }
    
    private void sendToChatbot(String message, ChatMessage loadingMessage) {
        ChatRequest request = new ChatRequest(message, currentUserId);
        request.setRideType("general");
        request.setLocation("current");
        
        Call<ChatResponse> call = apiService.sendRideAssistanceMessage(request);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    if (chatResponse.isSuccess()) {
                        // Update loading message with actual response
                        loadingMessage.setMessage(chatResponse.getReply());
                        loadingMessage.setTimestamp(chatResponse.getTimestamp());
                        chatAdapter.notifyDataSetChanged();
                    } else {
                        // Show error message
                        loadingMessage.setMessage("Sorry, I couldn't process your request. Please try again.");
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(AssistantChatActivity.this, 
                            "Error: " + chatResponse.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show error message
                    loadingMessage.setMessage("Sorry, I'm having trouble connecting. Please check your internet connection.");
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(AssistantChatActivity.this, 
                        "Network error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                
                // Scroll to bottom
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
            
            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                // Show error message
                loadingMessage.setMessage("Sorry, I'm having trouble connecting. Please try again later.");
                chatAdapter.notifyDataSetChanged();
                Toast.makeText(AssistantChatActivity.this, 
                    "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Scroll to bottom
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }
} 