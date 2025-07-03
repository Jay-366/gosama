#!/bin/bash
echo "Starting GoSama AI Backend..."
cd ai-backend
echo
echo "Backend location: $(pwd)"
echo
npm start 

messages: [
  { 
    role: 'system', 
    content: 'You are GoSama Assistant, a helpful and friendly AI support agent...' 
  },
  { role: 'user', content: userMessage }
]

if (msg.includes('book') || msg.includes('ride')) {
  mockReply = "To book a ride:\n1. Tap 'Find Ride'...";
}

// Add user context, location, ride history, etc.
private String context;
private String userPreferences;

// You can modify the welcome messages
private void addWelcomeMessage() {
    String welcomeMessage = "Welcome to GoSama AI Assistant! I can help you with...";
    // Custom prompt engineering here
}

