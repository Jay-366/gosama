# AI Backend for GoSama

This is the AI chatbot backend for the GoSama ride-sharing app. It provides two main endpoints for different types of chat assistance.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Set up environment variables:
Create a `.env` file in the ai-backend directory with:
```
OPENROUTER_API_KEY=your_openrouter_api_key_here
```

Get your API key from: https://openrouter.ai/keys

3. Start the server:
```bash
npm start
```

The server will run on http://localhost:3000

## API Endpoints

### POST /chat
General support chat endpoint for the GoSama app.

**Request:**
```json
{
  "message": "User's message"
}
```

**Response:**
```json
{
  "reply": "AI assistant's response"
}
```

### POST /ride-assistance
Specialized ride assistance endpoint with context-aware responses for ride-related queries.

**Request:**
```json
{
  "message": "User's ride-related question"
}
```

**Response:**
```json
{
  "reply": "Ride-specific assistant response"
}
```

### GET /
Health check endpoint that returns "AI Backend is running!"

## Usage with Android App

The GoSama Android app is configured to connect to this backend:
- For Android Emulator: `http://10.0.2.2:3000/`
- For Real Device: Update the IP address in `RetrofitClient.java` to your server's IP

## Features

- OpenRouter API integration with GPT-3.5-turbo
- CORS enabled for cross-origin requests
- Error handling and logging
- Context-aware responses for ride assistance
- Health check endpoint 