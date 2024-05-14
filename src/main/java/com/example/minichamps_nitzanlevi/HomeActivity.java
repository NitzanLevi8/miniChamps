package com.example.minichamps_nitzanlevi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import Adapters.EmptyChildAdapter;
import Models.Child;

public class HomeActivity extends AppCompatActivity {

    private ImageButton home_BTN_editProfile;
    private ImageButton home_BTN_tasks;
    private ImageButton home_BTN_giftStore;
    private ImageButton home_BTN_logout;
    private ImageButton home_BTN_children;
    private TextView home_TXT_heyUser;
    private ImageView home_IMG_profile;

    private RecyclerView home_RCLV_children;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();
        initViews();
        initializeDatabaseReference();
        ViewUserDetails();
        initializeChildrenRecyclerView();
    }

    private void findViews() {
        home_BTN_editProfile = findViewById(R.id.home_BTN_editProfile);
        home_BTN_giftStore = findViewById(R.id.home_BTN_giftStore);
        home_BTN_logout = findViewById(R.id.home_BTN_logout);
        home_BTN_children = findViewById(R.id.home_BTN_children);
        home_TXT_heyUser = findViewById(R.id.home_TXT_heyUser);
        home_IMG_profile = findViewById(R.id.home_IMG_profile);
        home_BTN_tasks = findViewById(R.id.home_BTN_tasks);
        home_RCLV_children = findViewById(R.id.home_RCLV_children);
    }

    private void initViews() {

        home_BTN_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        home_BTN_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        home_BTN_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AllTasksActivity.class);
                startActivity(intent);
                finish();
            }
        });

        home_BTN_giftStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AllGiftsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        home_BTN_children.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SetChildrenActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initializeDatabaseReference() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        }
    }

    private void ViewUserDetails() {
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        if (name != null) {
                            home_TXT_heyUser.setText("Hey " + name + ", Welcome Back!");
                        }

                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get().load(profileImageUrl)
                                    .transform(new CircleTransformation())
                                    .into(home_IMG_profile);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
    private void initializeChildrenRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.home_RCLV_children);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference childrenRef = userRef.child("children");
        childrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Child> children = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    children.add(child);
                }
                EmptyChildAdapter adapter = new EmptyChildAdapter(children);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


}
