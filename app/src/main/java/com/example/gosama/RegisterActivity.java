package com.example.gosama;

import android.content.Intent;
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

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create user document in Firestore
                            User user = new User(
                                    firebaseUser.getUid(),
                                    username,
                                    email,
                                    0 // initial ride count
                            );

                            db.collection("users").document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User document created");
                                        Toast.makeText(RegisterActivity.this,
                                                "Registration successful", Toast.LENGTH_SHORT).show();
                                        finish(); // Return to login screen
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error creating user document", e);
                                        Toast.makeText(RegisterActivity.this,
                                                "Error creating user profile", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
