package com.example.gosama;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        usernameInput = findViewById(R.id.etFullName);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        confirmPasswordInput = findViewById(R.id.etConfirmPassword);
        MaterialButton btnRegister = findViewById(R.id.btnContinue);

        TextView tvLoginPrompt = findViewById(R.id.tvLoginPrompt);
        MaterialButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        MaterialButton btnAppleSignIn = findViewById(R.id.btnAppleSignIn);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginPrompt.setOnClickListener(v -> finish());

        btnGoogleSignIn.setOnClickListener(v -> {
            // Google Sign In is handled in MainActivity
            finish();
        });

        btnAppleSignIn.setOnClickListener(v -> {
            // Apple Sign In implementation would go here
            Toast.makeText(this, "Apple Sign In not implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void registerUser() {
        String username = usernameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // Validate inputs
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting registration for email: " + email);
        
        // Show progress indicator
        findViewById(R.id.progressBar).setVisibility(android.view.View.VISIBLE);
        
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            findViewById(R.id.progressBar).setVisibility(android.view.View.GONE);
            showNetworkErrorDialog();
            return;
        }
        
        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide progress indicator
                    findViewById(R.id.progressBar).setVisibility(android.view.View.GONE);
                    
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Firebase user created with UID: " + firebaseUser.getUid());
                            
                            // Create user document in Firestore
                            User user = new User(
                                    firebaseUser.getUid(),
                                    username,
                                    email,
                                    0 // initial ride count
                            );
                            
                            Log.d(TAG, "Attempting to save user to Firestore: " + user.getUsername());

                            db.collection("users").document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User document successfully created in Firestore");
                                        Toast.makeText(RegisterActivity.this,
                                                "Registration successful", Toast.LENGTH_SHORT).show();
                                        finish(); // Return to login screen
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error creating user document in Firestore", e);
                                        Toast.makeText(RegisterActivity.this,
                                                "Error creating user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Log.e(TAG, "Firebase user is null after successful authentication");
                            Toast.makeText(RegisterActivity.this, "Error: Could not get user details", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Authentication failed", task.getException());
                        
                        // Handle specific error types
                        if (task.getException() instanceof com.google.firebase.FirebaseNetworkException) {
                            showNetworkErrorDialog();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void showNetworkErrorDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Network Error");
        builder.setMessage("Unable to connect to Firebase. Please check your internet connection and try again. If you're on WiFi, try switching to mobile data.");
        builder.setPositiveButton("Retry", (dialog, which) -> {
            dialog.dismiss();
            registerUser();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);
        builder.show();
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities == null) {
                return false;
            }
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        } else {
            // For older devices
            return connectivityManager.getActiveNetworkInfo() != null && 
                   connectivityManager.getActiveNetworkInfo().isConnected();
        }
    }
}
