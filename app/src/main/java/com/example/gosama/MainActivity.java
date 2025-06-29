package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onStart() {
        super.onStart();
        // We'll handle login manually through the login button
        // instead of auto-redirecting
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegisterPrompt = findViewById(R.id.tvRegisterPrompt);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        MaterialButton btnAppleSignIn = findViewById(R.id.btnAppleSignIn);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);

        tvRegisterPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, 
                                    "Password reset email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, 
                                    "Failed to send reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(MainActivity.this, 
                    "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Login successful, navigate to HomeActivity
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish(); // Close login activity
                            } else {
                                Toast.makeText(MainActivity.this, 
                                    "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(MainActivity.this, 
                    "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        // Got Google account, create or update user
                        String email = account.getEmail();
                        String displayName = account.getDisplayName();
                        
                        // Create user document in Firestore
                        String userId = email.replace("@", "_at_").replace(".", "_dot_");
                        User user = new User(userId, displayName, email, 0);
                        
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User document created/updated");
                                // Navigate to HomeActivity
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish(); // Close login activity
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error creating user document", e);
                                Toast.makeText(MainActivity.this,
                                    "Error creating user profile", Toast.LENGTH_SHORT).show();
                            });
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        btnAppleSignIn.setOnClickListener(v -> {
            Toast.makeText(this, "Apple Sign In not implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close login activity
        }
    }
}