package com.example.minichamps_nitzanlevi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapters.EmptyGiftAdapter;
import Models.Child;
import Models.Gift;

public class AllGiftsActivity extends AppCompatActivity {

    private RecyclerView allGifts_RCLV_gifts;
    private ImageButton allGifts_BTN_newGift;
    private ImageButton allGifts_BTN_homepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_gifts);

        findViews();
        initViews();

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        allGifts_RCLV_gifts.setLayoutManager(new LinearLayoutManager(this));

        List<Gift> gifts = retrieveGiftsFromDatabase();
        if (gifts.isEmpty()) {
            allGifts_RCLV_gifts.setAdapter(new EmptyGiftAdapter(new ArrayList<>()));
        } else {
            EmptyGiftAdapter giftAdapter = new EmptyGiftAdapter(gifts);
            allGifts_RCLV_gifts.setAdapter(giftAdapter);
        }
    }

    private List<Gift> retrieveGiftsFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("gifts");

        List<Gift> gifts = new ArrayList<>();
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot giftSnapshot : dataSnapshot.getChildren()) {
                    String id = giftSnapshot.getKey();
                    String name = giftSnapshot.child("name").getValue(String.class);
                    String description = giftSnapshot.child("description").getValue(String.class);
                    Integer coinsValue = giftSnapshot.child("coinsValue").getValue(Integer.class);
                    int coins = (coinsValue != null) ? coinsValue.intValue() : 0;
                    Gift gift = new Gift(id, name, description, coins);
                    gifts.add(gift);
                }

                if (gifts.isEmpty()) {
                    allGifts_RCLV_gifts.setAdapter(new EmptyGiftAdapter(new ArrayList<>()));
                } else {
                    allGifts_RCLV_gifts.setAdapter(new EmptyGiftAdapter(gifts));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", "Failed to retrieve gifts: " + databaseError.getMessage());
            }
        });

        return gifts;
    }



    private void findViews() {
        allGifts_RCLV_gifts = findViewById(R.id.allGifts_RCLV_gifts);
        allGifts_BTN_newGift = findViewById(R.id.allGifts_BTN_newGift);
        allGifts_BTN_homepage = findViewById(R.id.allGifts_BTN_homePage);
    }

    private void initViews() {
        allGifts_BTN_newGift.setOnClickListener(v -> {
            Intent intent = new Intent(AllGiftsActivity.this, SetNewGiftActivity.class);
            startActivity(intent);
            finish();
        });

        allGifts_BTN_homepage.setOnClickListener(v -> {
            Intent intent = new Intent(AllGiftsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void showChildSelectionDialog(List<Child> childrenList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_select_child, null);
        builder.setView(dialogView);

        Spinner spinnerChildren = dialogView.findViewById(R.id.spinner_children);

        List<String> childrenNames = getChildNames(childrenList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, childrenNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChildren.setAdapter(adapter);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedChild = spinnerChildren.getSelectedItem().toString();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<String> getChildNames(List<Child> childrenList) {
        List<String> names = new ArrayList<>();
        for (Child child : childrenList) {
            names.add(child.getName());
        }
        return names;
    }

}
