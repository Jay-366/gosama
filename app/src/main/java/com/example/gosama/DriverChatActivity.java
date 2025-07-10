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
import com.example.gosama.viewmodel.UserViewModel;
import java.util.UUID;

public class DriverChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private UserViewModel userViewModel;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_chat);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewAssistantMessages);
        editTextMessage = findViewById(R.id.editTextAssistantMessage);
        buttonSend = findViewById(R.id.buttonAssistantSend);

        // Initialize UserViewModel
        userViewModel = new UserViewModel();
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUserId = user.getUid();
            } else {
                currentUserId = null;
            }
        });
        userViewModel.loadUserData();
        currentUserId = "anonymous";

        // Setup RecyclerView
        setupRecyclerView();
        setupClickListeners();
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
            "Hi, this is Kangyan Ong. How can I help you?",
            false
        );
        welcomeMessage.setType("driver");
        chatAdapter.addMessage(welcomeMessage);
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        editTextMessage.setText("");
        ChatMessage userMessage = new ChatMessage(message, true);
        userMessage.setUserId(currentUserId);
        userMessage.setType("user");
        chatAdapter.addMessage(userMessage);
        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        // Simulate driver reply
        recyclerView.postDelayed(() -> {
            ChatMessage reply = new ChatMessage("I'll get back to you soon!", false);
            reply.setType("driver");
            chatAdapter.addMessage(reply);
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }, 1000);
    }
} 