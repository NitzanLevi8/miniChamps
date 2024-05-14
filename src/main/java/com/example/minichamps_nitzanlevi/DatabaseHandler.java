package com.example.minichamps_nitzanlevi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseHandler {
    private static final DatabaseHandler instance = new DatabaseHandler();
    private DatabaseReference userRef;

    private DatabaseHandler() {
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    public static DatabaseHandler getInstance() {
        return instance;
    }

    public void updateUserProfileImageUrl(String userId, String imageUrl) {
        if (userRef != null) {
            userRef.child(userId).child("profilePictureUrl").setValue(imageUrl);
        }
    }
}
