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
  
  // ENHANCED PROMPT ENGINEERING: More sophisticated response logic
  console.log('Using enhanced mock response system...');
  
  // Improved mock responses with better prompt engineering
  let mockReply;
  const msg = userMessage.toLowerCase();
  
  if (msg.includes('hello') || msg.includes('hi') || msg.includes('hey')) {
    mockReply = "Hello! 👋 I'm your GoSama support assistant. I'm here to help you with:\n\n🚗 Ride booking and management\n👤 Account and profile settings\n💳 Payment and billing questions\n🛠️ App troubleshooting\n📍 Location and navigation help\n\nWhat can I assist you with today?";
  } else if (msg.includes('ride') || msg.includes('book')) {
    mockReply = "🚗 **Ride Booking Help**\n\nTo book a ride with GoSama:\n1. Tap 'Find Ride' on the home screen\n2. Set your pickup location (or use current location)\n3. Enter your destination\n4. Choose ride type (Standard, Premium, etc.)\n5. Review fare estimate\n6. Confirm booking\n\nNeed help with a specific step? Just ask!";
  } else if (msg.includes('help') || msg.includes('support')) {
    mockReply = "🆘 **GoSama Support Menu**\n\nI can help you with:\n\n🚗 **Rides**: Booking, canceling, tracking\n👤 **Account**: Profile, settings, verification\n💳 **Payments**: Cards, billing, receipts\n📱 **App Issues**: Login, crashes, features\n🗺️ **Navigation**: Locations, addresses\n⭐ **Ratings**: Driver feedback, ride history\n\nWhat specific area do you need help with?";
  } else if (msg.includes('profile') || msg.includes('account')) {
    mockReply = "👤 **Profile & Account Help**\n\nTo manage your GoSama profile:\n• Tap the Profile icon in bottom navigation\n• Update: Name, photo, phone number, email\n• Verify: Phone and email for security\n• Settings: Notifications, privacy, preferences\n\nSpecific profile question? I'm here to help!";
  } else if (msg.includes('payment') || msg.includes('card') || msg.includes('bill')) {
    mockReply = "💳 **Payment & Billing Support**\n\nFor payment help:\n• Add/remove payment methods in Profile > Payment\n• View ride history and receipts in 'My Rides'\n• Contact support for billing disputes\n• Set up automatic payments for convenience\n\nWhat payment issue can I help resolve?";
  } else if (msg.includes('driver') || msg.includes('track')) {
    mockReply = "🚗 **Driver & Tracking Help**\n\nDuring your ride:\n• Track driver location in real-time\n• See estimated arrival time\n• Contact driver via app (call/message)\n• Share trip details with friends/family\n• Rate and review after completion\n\nHaving issues with a current ride?";
  } else if (msg.includes('cancel') || msg.includes('refund')) {
    mockReply = "❌ **Cancellation & Refund Info**\n\nTo cancel a ride:\n• Go to 'My Rides' > Active rides\n• Tap 'Cancel Ride'\n• Check cancellation fee policy\n• Refunds processed within 3-5 business days\n\n⚠️ Note: Fees may apply based on timing and driver proximity.";
  } else {
    mockReply = `🤔 I understand you're asking about: "${userMessage}"\n\nI'm your GoSama assistant, specialized in helping with:\n• Ride booking and management\n• Account and profile issues\n• Payment and billing\n• App troubleshooting\n\nCould you provide more details about what specific help you need? I'm here to make your GoSama experience smooth! 🚗✨`;
  }

  // Simulate a small delay like a real API
  setTimeout(() => {
    res.json({ reply: mockReply });
  }, 500);
  
  /* REAL API CODE WITH SYSTEM PROMPT (uncomment when API key issue is fixed):
  const apiKey = process.env.OPENROUTER_API_KEY?.trim();
  
  try {
    const response = await axios.post(
      'https://openrouter.ai/api/v1/chat/completions',
      {
        model: 'openai/gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: 'You are GoSama Assistant, a helpful and friendly AI support agent for the GoSama ride-sharing app. Your role is to help users with ride booking, account management, payments, and general app support. Be concise, helpful, and always maintain a positive tone. Use emojis sparingly but appropriately. Focus on practical solutions and clear step-by-step guidance.'
          },
          { role: 'user', content: userMessage }
        ],
        temperature: 0.7,
        max_tokens: 300
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
  
  // ENHANCED RIDE ASSISTANCE: Specialized prompts for ride-specific help
  console.log('Using enhanced ride assistance system...');
  
  let mockReply;
  const msg = userMessage.toLowerCase();
  
  if (msg.includes('hello') || msg.includes('hi') || msg.includes('hey')) {
    mockReply = "🚗 **Welcome to GoSama Ride Assistant!**\n\nI'm your dedicated ride companion. I can help you with:\n\n📍 **Quick Booking**: Fast ride setup\n🔍 **Driver Matching**: Find nearby drivers\n📱 **Live Tracking**: Real-time ride monitoring\n🧭 **Navigation**: Route optimization\n💰 **Fare Info**: Pricing and payment\n🛑 **Ride Management**: Cancel, modify, reschedule\n\nReady to get you moving! What do you need?";
  } else if (msg.includes('book') || msg.includes('ride') || msg.includes('start')) {
    mockReply = "🚗 **Quick Ride Booking**\n\n**Step-by-Step:**\n1️⃣ Tap 'Find Ride' on home screen\n2️⃣ Set pickup location 📍\n   • Use current location\n   • Search address\n   • Drop pin on map\n3️⃣ Enter destination 🎯\n4️⃣ Choose ride type:\n   • 🚗 Standard (Budget-friendly)\n   • ⭐ Premium (Comfort)\n   • 🚐 XL (Group rides)\n5️⃣ Review fare estimate 💰\n6️⃣ Confirm booking ✅\n\n**Need help with any step?** Just ask!";
  } else if (msg.includes('driver') || msg.includes('find') || msg.includes('match')) {
    mockReply = "🔍 **Driver Matching Process**\n\n**How it works:**\n• 📡 System finds nearest drivers\n• ⏱️ Average wait: 3-8 minutes\n• 📱 You'll see driver details:\n  - Name and photo\n  - Vehicle info & license\n  - ETA to your location\n  - Driver rating ⭐\n\n**Live tracking:**\n• 🗺️ Real-time driver location\n• 📞 Direct call/message\n• 🕐 Accurate arrival time\n\nLooking for a ride now?";
  } else if (msg.includes('cancel') || msg.includes('stop')) {
    mockReply = "🛑 **Ride Cancellation**\n\n**How to cancel:**\n1. Go to 'My Rides' 📱\n2. Find active booking\n3. Tap 'Cancel Ride' ❌\n4. Confirm cancellation\n\n**Cancellation Policy:**\n• 🆓 Free cancellation (first 2 minutes)\n• 💰 Small fee if driver assigned\n• 💳 Refund processed in 1-3 days\n\n**Emergency?** Contact support immediately!\nNeed to cancel a current ride?";
  } else if (msg.includes('price') || msg.includes('cost') || msg.includes('fare') || msg.includes('payment')) {
    mockReply = "💰 **Fare & Payment Info**\n\n**Pricing factors:**\n• 📏 Distance traveled\n• ⏰ Time duration\n• 📈 Demand level\n• 🌆 Area surcharges\n\n**Payment methods:**\n• 💳 Credit/Debit cards\n• 📱 Digital wallets\n• 💵 Cash (select areas)\n• 🎁 Promo codes\n\n**Fare estimate** shown before booking!\n**Receipts** sent automatically.\n\nQuestion about a specific fare?";
  } else if (msg.includes('track') || msg.includes('location') || msg.includes('where')) {
    mockReply = "📍 **Live Ride Tracking**\n\n**Real-time features:**\n• 🗺️ Driver location on map\n• 🧭 Route being taken\n• ⏱️ Updated arrival time\n• 📱 Share trip with contacts\n\n**During ride:**\n• 📞 Call driver directly\n• 💬 Send messages\n• 🚨 Emergency button\n• ⭐ Rate experience\n\n**Safety first!** All rides monitored.\nNeed help with a current ride?";
  } else if (msg.includes('emergency') || msg.includes('help') || msg.includes('safety')) {
    mockReply = "🚨 **Safety & Emergency**\n\n**Immediate help:**\n• 🆘 Emergency button in app\n• 📞 24/7 support hotline\n• 👮 Police contact option\n• 📱 Share ride details instantly\n\n**Safety features:**\n• ✅ Driver verification\n• 🛡️ Trip monitoring\n• 📍 GPS tracking\n• ⭐ Rating system\n\n**Emergency? Call local authorities first!**\nApp support for non-emergency issues.";
  } else {
    mockReply = `🚗 **Ride Assistant Ready!**\n\nYou asked about: "${userMessage}"\n\nI specialize in:\n• 🚀 **Quick bookings** and ride setup\n• 🔍 **Driver coordination** and tracking\n• 💰 **Fare management** and payments\n• 🛠️ **Ride troubleshooting**\n• 🧭 **Route and navigation** help\n\nWhat specific ride assistance do you need? I'm here to make your journey smooth! ✨`;
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
