package com.example.minichamps_nitzanlevi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import Models.Child;

public class SetChildrenActivity extends AppCompatActivity {

    private EditText setChild_TXT_childName;
    private ImageButton setChild_BTN_addChild;
    private ImageButton setChild_BTN_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_children); // Correct layout file

        findViews();
        initViews();
    }

    private void findViews() {
        setChild_TXT_childName = findViewById(R.id.setChild_TXT_childName);
        setChild_BTN_addChild = findViewById(R.id.setChild_BTN_addChild);
        setChild_BTN_next = findViewById(R.id.setChild_BTN_home);
    }


    private void initViews() {
        setChild_BTN_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextActivity();
            };
        });


        setChild_BTN_addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChildToFirebase();
            };
        });
    }

    private void moveToNextActivity() {
        Intent intent = new Intent(SetChildrenActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void addChildToFirebase() {
        String childName = setChild_TXT_childName.getText().toString().trim();

        //creating a new child in DB
        if (!childName.isEmpty()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            String childId = userRef.child("children").push().getKey();
            Child child = new Child(childName, 0);
            if (childId != null) {
                userRef.child("children").child(childId).setValue(child);

                // success case
                Toast.makeText(SetChildrenActivity.this, "Child added successfully", Toast.LENGTH_SHORT).show();

                // Clear input field
                setChild_TXT_childName.setText("");
            } else {
                // error case
                Toast.makeText(SetChildrenActivity.this, "Failed to add child", Toast.LENGTH_SHORT).show();
            }
        } else {
            // a case of child name is empty
            Toast.makeText(SetChildrenActivity.this, "Please enter child name", Toast.LENGTH_SHORT).show();
        }
    }

}