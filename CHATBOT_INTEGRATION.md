# GoSama Chatbot Integration Guide

This document explains how the AI chatbot has been integrated into the GoSama Android app.

## 🏗️ Architecture Overview

The chatbot integration consists of:

### Backend (Node.js/Express)
- **Location**: `ai-chatbot-backend/`
- **API Endpoints**: `/chat`, `/ride-assistance`, `/health`, `/models`
- **AI Provider**: OpenRouter API with GPT-3.5-turbo

### Android App
- **Activities**: `AssistantChatActivity`, `ChatActivity`
- **Network**: Retrofit for API communication
- **UI**: RecyclerView with custom adapters

## 📱 Android Implementation

### 1. Data Models

#### `ChatMessage.java`
Represents individual chat messages:
```java
public class ChatMessage {
    private String id;
    private String message;
    private String reply;
    private String userId;
    private String type; // "user", "assistant", "ride_assistance"
    private Date timestamp;
    private boolean isUserMessage;
}
```

#### `ChatRequest.java`
API request model:
```java
public class ChatRequest {
    private String message;
    private String userId;
    private String model;
    private double temperature;
    private String rideType;
    private String location;
}
```

#### `ChatResponse.java`
API response model with error handling:
```java
public class ChatResponse {
    private String reply;
    private String model;
    private String type;
    private Date timestamp;
    private Usage usage;
    private String error;
    private String details;
}
```

### 2. Network Layer

#### `ChatbotApiService.java`
Retrofit interface for API calls:
```java
@POST("chat")
Call<ChatResponse> sendChatMessage(@Body ChatRequest request);

@POST("ride-assistance")
Call<ChatResponse> sendRideAssistanceMessage(@Body ChatRequest request);
```

#### `RetrofitClient.java`
Singleton for network configuration:
```java
private static final String BASE_URL = "http://10.0.2.2:3000/"; // Emulator
// Use "http://your-server-ip:3000/" for real device
```

### 3. UI Components

#### `ChatAdapter.java`
RecyclerView adapter with different view types for user and assistant messages.

#### Layout Files
- `item_message_user.xml` - Blue bubble for user messages
- `item_message_assistant.xml` - White bubble for assistant messages
- Background drawables for message styling

### 4. Activities

#### `AssistantChatActivity.java`
Specialized for ride assistance:
- Uses `/ride-assistance` endpoint
- Context-aware system messages
- Ride-specific welcome message

#### `ChatActivity.java`
General support chat:
- Uses `/chat` endpoint
- General support topics
- Standard welcome message

## 🔧 Configuration

### Backend Setup
1. Set environment variable:
   ```bash
   export OPENROUTER_API_KEY="your-api-key-here"
   ```

2. Start server:
   ```bash
   cd ai-chatbot-backend
   npm start
   ```

### Android Configuration
1. Update `RetrofitClient.java` with correct server URL:
   - Emulator: `http://10.0.2.2:3000/`
   - Real device: `http://your-server-ip:3000/`

2. Ensure internet permission in `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

## 🚀 Features

### AssistantChatActivity (Ride Assistance)
- **Purpose**: Help users with ride booking and navigation
- **Endpoint**: `/ride-assistance`
- **Context**: Ride-specific assistance
- **Welcome Message**: Lists ride-related help topics

### ChatActivity (General Support)
- **Purpose**: General app support and troubleshooting
- **Endpoint**: `/chat`
- **Context**: General assistance
- **Welcome Message**: Lists support topics

### Common Features
- Real-time chat interface
- Loading indicators ("Typing...")
- Error handling with user-friendly messages
- Auto-scroll to latest message
- User identification via UserViewModel
- Timestamp display

## 🔄 API Flow

1. **User sends message** → Android app
2. **Android app** → Creates `ChatRequest` with user ID
3. **Backend** → Receives request, calls OpenRouter API
4. **OpenRouter** → Returns AI response
5. **Backend** → Formats response, returns to Android
6. **Android app** → Updates UI with response

## 🛡️ Error Handling

### Network Errors
- Connection timeouts
- Server unavailable
- Invalid responses

### API Errors
- Invalid API key
- Rate limiting
- Malformed requests

### User Experience
- Loading indicators
- Error messages in chat
- Toast notifications for critical errors
- Graceful degradation

## 📊 Monitoring

### Backend Logs
- Request/response logging with timestamps
- User identification in logs
- Error details for debugging

### Android Logs
- Network call results
- UI state changes
- Error conditions

## 🔧 Development

### Testing
```bash
# Test backend
cd ai-chatbot-backend
npm test

# Test individual endpoints
curl -X POST http://localhost:3000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello", "userId": "test"}'
```

### Debugging
1. Check server logs for API errors
2. Verify API key configuration
3. Test network connectivity
4. Check Android logs for Retrofit errors

## 🚀 Deployment

### Production Considerations
1. **Security**: Use HTTPS in production
2. **API Key**: Store securely, rotate regularly
3. **Rate Limiting**: Monitor usage, adjust limits
4. **Error Monitoring**: Implement proper logging
5. **Backup**: Regular database backups if storing chat history

### Environment Variables
```bash
# Production
export NODE_ENV=production
export OPENROUTER_API_KEY="your-production-key"
export PORT=3000
```

## 📈 Future Enhancements

### Potential Features
- Chat history persistence
- File/image sharing
- Voice messages
- Multi-language support
- Advanced ride booking integration
- Payment assistance
- Driver chat integration

### Technical Improvements
- WebSocket for real-time communication
- Message encryption
- Offline message queuing
- Push notifications
- Analytics and insights

## 🆘 Troubleshooting

### Common Issues

1. **"API key not configured"**
   - Set `OPENROUTER_API_KEY` environment variable
   - Restart server

2. **"Connection refused"**
   - Check if server is running
   - Verify port 3000 is available
   - Check firewall settings

3. **Android can't connect**
   - Verify server URL in `RetrofitClient.java`
   - Check internet permissions
   - Test with emulator vs real device

4. **Slow responses**
   - Check OpenRouter API status
   - Monitor rate limits
   - Consider upgrading API plan

## 📞 Support

For issues or questions:
1. Check server logs for error details
2. Verify API key and configuration
3. Test endpoints manually
4. Review network connectivity
5. Check OpenRouter API documentation 