console.log("Starting server.js...");
const express = require('express');
const axios = require('axios');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

app.post('/chat', async (req, res) => {
  const userMessage = req.body.message;
  console.log('Received chat request:', userMessage);
  
  // TEMPORARY: Mock response for testing while API key issue is resolved
  console.log('Using mock response for testing...');
  
  // Simple mock responses based on user input
  let mockReply;
  const msg = userMessage.toLowerCase();
  
  if (msg.includes('hello') || msg.includes('hi')) {
    mockReply = "Hello! I'm your GoSama support assistant. How can I help you today?";
  } else if (msg.includes('ride') || msg.includes('book')) {
    mockReply = "I can help you with ride booking! To book a ride, go to the 'Find Ride' section and enter your pickup and destination locations.";
  } else if (msg.includes('help')) {
    mockReply = "I'm here to help! I can assist you with:\n• Booking rides\n• Account questions\n• App features\n• Troubleshooting\n\nWhat would you like to know?";
  } else if (msg.includes('profile')) {
    mockReply = "To update your profile, tap the Profile icon in the bottom navigation. You can edit your name, photo, and contact information there.";
  } else {
    mockReply = `I understand you're asking about: "${userMessage}". I'm here to help with GoSama app questions, ride booking, and general support. What specific assistance do you need?`;
  }

  // Simulate a small delay like a real API
  setTimeout(() => {
    res.json({ reply: mockReply });
  }, 500);
  
  /* REAL API CODE (uncomment when API key issue is fixed):
  const apiKey = process.env.OPENROUTER_API_KEY?.trim();
  
  try {
    const response = await axios.post(
      'https://openrouter.ai/api/v1/chat/completions',
      {
        model: 'openai/gpt-3.5-turbo',
        messages: [{ role: 'user', content: userMessage }]
      },
      {
        headers: {
          'Authorization': `Bearer ${apiKey}`,
          'Content-Type': 'application/json',
          'HTTP-Referer': 'http://localhost:3000',
          'X-Title': 'GoSama Chatbot'
        },
        timeout: 30000
      }
    );
    res.json({ reply: response.data.choices[0].message.content });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
  */
});

app.post('/ride-assistance', async (req, res) => {
  const userMessage = req.body.message;
  console.log('Received ride assistance request:', userMessage);
  
  // TEMPORARY: Mock response for ride assistance
  console.log('Using mock ride assistance response...');
  
  let mockReply;
  const msg = userMessage.toLowerCase();
  
  if (msg.includes('hello') || msg.includes('hi')) {
    mockReply = "Hi! I'm your GoSama ride assistant. I can help you with booking rides, finding drivers, tracking your ride, and navigation. What do you need help with?";
  } else if (msg.includes('book') || msg.includes('ride')) {
    mockReply = "To book a ride:\n1. Tap 'Find Ride' on the home screen\n2. Enter your pickup location\n3. Enter your destination\n4. Choose your ride type\n5. Confirm and book!\n\nWould you like help with any specific step?";
  } else if (msg.includes('driver') || msg.includes('find')) {
    mockReply = "I'll help you find a driver! Once you book a ride, I'll match you with the nearest available driver. You can track their location and estimated arrival time in real-time.";
  } else if (msg.includes('cancel')) {
    mockReply = "To cancel your ride, go to 'My Rides' and tap the cancel button. Note: Cancellation fees may apply depending on timing.";
  } else if (msg.includes('price') || msg.includes('cost')) {
    mockReply = "Ride prices depend on distance, time, and demand. You'll see the estimated fare before confirming your booking. We accept various payment methods for your convenience.";
  } else {
    mockReply = `I'm here to help with your ride needs! Your question about "${userMessage}" - I can assist with booking, tracking, payments, and driver coordination. What specific help do you need?`;
  }

  // Simulate API delay
  setTimeout(() => {
    res.json({ reply: mockReply });
  }, 600);
});

app.get('/', (req, res) => {
  res.send('AI Backend is running!');
});

// Test endpoint without OpenRouter API
app.post('/test', (req, res) => {
  const userMessage = req.body.message;
  console.log('Test endpoint received:', userMessage);
  res.json({ reply: `Echo: ${userMessage}` });
});

// Simple API key test endpoint
app.post('/test-api', async (req, res) => {
  try {
    console.log('Testing OpenRouter API key...');
    console.log('Using API key:', process.env.OPENROUTER_API_KEY ? process.env.OPENROUTER_API_KEY.substring(0, 15) + '...' : 'undefined');
    
    const response = await axios.post(
      'https://openrouter.ai/api/v1/chat/completions',
      {
        model: 'openai/gpt-3.5-turbo',
        messages: [{ role: 'user', content: 'Hello' }],
        max_tokens: 10
      },
      {
        headers: {
          'Authorization': `Bearer ${process.env.OPENROUTER_API_KEY}`,
          'Content-Type': 'application/json'
        },
        timeout: 10000
      }
    );
    
    res.json({ success: true, response: response.data.choices[0].message.content });
  } catch (error) {
    console.error('API Test Error:', error.response?.data || error.message);
    res.status(500).json({ 
      success: false, 
      error: error.response?.data || error.message,
      status: error.response?.status 
    });
  }
});

app.listen(3000, () => console.log('Backend running on http://localhost:3000'));
