console.log("Starting server.js...");
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

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

// Get user ride count
app.get('/user/:uid/rideCount', async (req, res) => {
  try {
    const { uid } = req.params;
    const userDoc = await db.collection('users').doc(uid).get();
    if (!userDoc.exists) return res.status(404).json({ error: 'User not found' });
    res.json({ rideCount: userDoc.data().rideCount });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get available rides by pickup, dropoff, and date
app.get('/rides', async (req, res) => {
  try {
    const { pickup, dropoff, date } = req.query;
    const ridesRef = db.collection('Rides');
    const snapshot = await ridesRef
      .where('pickupAddress', '==', pickup)
      .where('dropoffAddress', '==', dropoff)
      .where('departureDate', '==', date)
      .get();

    const rides = [];
    snapshot.forEach(doc => rides.push({ id: doc.id, ...doc.data() }));
    res.json({ rides });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get user's confirmed rides
app.get('/user/:uid/confirmedRides', async (req, res) => {
  try {
    const { uid } = req.params;
    const ridesRef = db.collection('confirmed_rides').where('passengerId', '==', uid);
    const snapshot = await ridesRef.get();
    const rides = [];
    snapshot.forEach(doc => rides.push({ id: doc.id, ...doc.data() }));
    res.json({ rides });
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

// Get ride details by rideId
app.get('/rides/:rideId', async (req, res) => {
  try {
    const { rideId } = req.params;
    const rideDoc = await db.collection('Rides').doc(rideId).get();
    if (!rideDoc.exists) return res.status(404).json({ error: 'Ride not found' });
    res.json({ id: rideDoc.id, ...rideDoc.data() });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Start server
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`AI Backend listening on port ${PORT}`);
});
