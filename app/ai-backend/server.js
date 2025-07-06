console.log("Starting server.js...");
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');
require('dotenv').config();
const axios = require('axios');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const app = express();
app.use(cors());
app.use(express.json());

// Health check
app.get('/', (req, res) => {
  res.send('AI Backend is running!');
});

// Get user profile data
app.get('/user/:uid', async (req, res) => {
  try {
    const { uid } = req.params;
    const userDoc = await db.collection('users').doc(uid).get();
    if (!userDoc.exists) return res.status(404).json({ error: 'User not found' });
    res.json({ user: userDoc.data() });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get user's parcels
app.get('/user/:uid/parcels', async (req, res) => {
  try {
    const { uid } = req.params;
    const parcelsRef = db.collection('parcels').where('userId', '==', uid);
    const snapshot = await parcelsRef.get();
    const parcels = [];
    snapshot.forEach(doc => parcels.push({ id: doc.id, ...doc.data() }));
    res.json({ parcels });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Chat endpoint (handles general questions, user data, and parcel data)
app.post('/chat', async (req, res) => {
  const { message, userId, history } = req.body;
  
  if (!userId) {
    return res.json({ reply: "Sorry, I couldn't identify your user. Please log in again.", success: false });
  }

  // Fetch user info
  let userDoc = null;
  let userData = null;
  try {
    userDoc = await db.collection('users').doc(userId).get();
    if (userDoc.exists) {
      userData = userDoc.data();
    } else {
      return res.json({ reply: "User not found in the database.", success: false });
    }
  } catch (err) {
    return res.json({ reply: `Error fetching user info: ${err.message}` , success: false });
  }

  // Fetch all parcels
  let parcels = [];
  try {
    const parcelsSnapshot = await db.collection('parcels').where('userId', '==', userId).get();
    parcels = parcelsSnapshot.docs.map(doc => doc.data());
    console.log('=== PARCEL DATA DEBUG ===');
    console.log('User ID:', userId);
    console.log('Number of parcels found:', parcels.length);
    console.log('Raw parcel data:', JSON.stringify(parcels, null, 2));
    console.log('=== END PARCEL DATA ===');
  } catch (e) {
    console.log('Error fetching parcels:', e.message);
  }

  // Build a rich system prompt
  const systemPrompt = `
You are a helpful assistant for the GoSama ride-sharing and parcel delivery app.

Here is the user's data:
User profile: ${JSON.stringify(userData)}
User parcels: ${JSON.stringify(parcels)}

When the user asks about their parcels, always include details such as pickup date, creation date, status, and description. If there are multiple parcels, list them all with their details.

Examples:
User: What is the pick up date of my parcel?
Assistant: Your parcel is scheduled for pickup on 2024-07-20 at 10:00 AM.

User: When was my parcel order created?
Assistant: Your parcel order was created on 2024-07-18 at 9:00 AM.

User: Check my parcel
Assistant: You have 1 parcel. Details: Description: Books, Status: pending, Pickup date: 2024-07-20, Created at: 2024-07-18.
`;

  try {
    // Build the conversation history for the AI
    const messages = [
      { role: "system", content: systemPrompt },
      ...(Array.isArray(history) ? history : []), // previous conversation turns
      { role: "user", content: message }
    ];
    
    const aiRes = await axios.post(
      'https://openrouter.ai/api/v1/chat/completions',
      {
        model: "openai/gpt-4o-mini-2024-07-18",
        messages: messages
      },
      {
        headers: {
          'Authorization': 'Bearer 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',
          'Content-Type': 'application/json'
        }
      }
    );
    
    const aiReply = aiRes.data.choices[0].message.content;
    console.log('AI raw reply:', aiReply);

    // Return the AI's answer directly
    return res.json({ reply: aiReply, success: true });

  } catch (err) {
    return res.json({ reply: `Error: ${err.message}`, success: false });
  }
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`AI Backend listening on port ${PORT}`);
});
