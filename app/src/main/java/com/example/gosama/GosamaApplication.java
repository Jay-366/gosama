package com.example.gosama;

import android.app.Application;
import android.util.Log;

import com.google.android.libraries.places.api.Places;
import com.google.firebase.FirebaseApp;

public class GosamaApplication extends Application {
    private static final String TAG = "GosamaApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase initialized");
        
        // Note: Firebase App Check is temporarily disabled to troubleshoot network connectivity issues
    }
}
