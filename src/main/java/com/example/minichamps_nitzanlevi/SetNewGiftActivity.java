// SetNewGiftActivity.java
package com.example.minichamps_nitzanlevi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Models.Gift;

public class SetNewGiftActivity extends AppCompatActivity {

    private EditText gift_TXT_giftName;
    private EditText gift_TXT_description;
    private EditText gift_TXT_coinsVal;
    private Button gift_BTN_createGift;
    private ImageButton gift_BTN_back;

    private DatabaseReference giftsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_gift);

        giftsRef = FirebaseDatabase.getInstance().getReference().child("gifts");

       findViews();
       initViews();

    }

    private void initViews() {
        gift_BTN_createGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGift();
            }
        });

        gift_BTN_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetNewGiftActivity.this, AllGiftsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void findViews(){
        gift_TXT_giftName = findViewById(R.id.gift_TXT_giftName);
        gift_TXT_description = findViewById(R.id.gift_TXT_description);
        gift_TXT_coinsVal = findViewById(R.id.gift_TXT_coinsVal);
        gift_BTN_createGift = findViewById(R.id.gift_BTN_createGift);
        gift_BTN_back = findViewById(R.id.gift_BTN_back);

    }
    private void createGift() {
        String name = gift_TXT_giftName.getText().toString();
        String description = gift_TXT_description.getText().toString();
        int coinsValue = Integer.parseInt(gift_TXT_coinsVal.getText().toString());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("gifts");
        String giftId = userGiftsRef.push().getKey();
        Gift gift = new Gift(giftId, name, description, coinsValue);
        userGiftsRef.child(giftId).setValue(gift, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(SetNewGiftActivity.this, "Gift created successfully", Toast.LENGTH_SHORT).show();
                    // Clear input fields
                    clearInputFields();
                } else {
                    Toast.makeText(SetNewGiftActivity.this, "Failed to create gift: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearInputFields() {
        gift_TXT_giftName.setText("");
        gift_TXT_description.setText("");
        gift_TXT_coinsVal.setText("");
    }
}
