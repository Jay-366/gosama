package com.example.gosama.model;

import java.util.Date;

public class ChatMessage {
    private String id;
    private String message;
    private String reply;
    private String userId;
    private String type; // "user", "assistant", "ride_assistance"
    private Date timestamp;
    private boolean isUserMessage;

    public ChatMessage() {
        this.timestamp = new Date();
    }

    public ChatMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.timestamp = new Date();
    }

    public ChatMessage(String message, String reply, boolean isUserMessage) {
        this.message = message;
        this.reply = reply;
        this.isUserMessage = isUserMessage;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public void setUserMessage(boolean userMessage) {
        isUserMessage = userMessage;
    }
} 