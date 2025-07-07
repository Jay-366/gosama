package com.example.gosama.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gosama.User;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadUserData() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        currentUser.setValue(user);
                        errorMessage.setValue(null); // Clear previous errors on success
                    } else {
                        errorMessage.setValue("User data could not be found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserViewModel", "Error loading user data", e);
                    if (e instanceof FirebaseNetworkException) {
                        errorMessage.setValue("A network error occurred. Please check your connection and try again.");
                    } else if (e instanceof FirebaseFirestoreException &&
                               ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                        errorMessage.setValue("Could not connect to the server. Please check your network settings.");
                    } else {
                        errorMessage.setValue("An unexpected error occurred while loading your data.");
                    }
                });
        }
    }

    public String getCurrentUserDisplayName() {
        User user = currentUser.getValue();
        return user != null ? user.getUsername() : "";
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        auth.signOut();
        currentUser.setValue(null);
    }
}
