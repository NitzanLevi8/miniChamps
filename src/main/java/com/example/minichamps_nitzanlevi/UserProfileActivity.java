package com.example.minichamps_nitzanlevi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView userProfile_IMG_profileImageView;
    private EditText userProfile_TXT_userName;
    private EditText userProfile_TXT_userEmail;
    private EditText userProfile_TXT_userPhoneNumber;
    private Button userProfile_BTN_saveChanges;
    private ImageButton userProfile_BTN_setProfileImage;

    // Firebase
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    public static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        findViews();
        initViews();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            fetchUserDetails();
        }
    }

    private void findViews() {
        userProfile_IMG_profileImageView = findViewById(R.id.userProfile_IMG_profileImageView);
        userProfile_TXT_userName = findViewById(R.id.userProfile_TXT_userName);
        userProfile_TXT_userEmail = findViewById(R.id.userProfile_TXT_userEmail);
        userProfile_TXT_userPhoneNumber = findViewById(R.id.userProfile_TXT_userPhoneNumber);
        userProfile_BTN_saveChanges = findViewById(R.id.userProfile_BTN_saveChanges);
        userProfile_BTN_setProfileImage = findViewById(R.id.userProfile_BTN_setProfileImage);
    }

    private void initViews() {
        // save changes button
        userProfile_BTN_saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
                moveToHomePage();
            }
        });

        // update photo button
        userProfile_BTN_setProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfilePhoto();
            }
        });
    }

    private void fetchUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    userProfile_TXT_userName.setText(name);
                    userProfile_TXT_userEmail.setText(email);
                    userProfile_TXT_userPhoneNumber.setText(phone);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Picasso.get().load(profileImageUrl).into(userProfile_IMG_profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveChanges() {
        String name = userProfile_TXT_userName.getText().toString();
        String email = userProfile_TXT_userEmail.getText().toString();
        String phone = userProfile_TXT_userPhoneNumber.getText().toString();
        updateUserDetailsToDB(name, email, phone);
    }

    private void updateUserDetailsToDB(String name, String email, String phone) {
        if (userRef != null) {
            userRef.child("name").setValue(name);
            userRef.child("email").setValue(email);
            userRef.child("phone").setValue(phone);
        }
    }

    private void updateProfilePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebaseStorage(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        String imageName = currentUser.getUid() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(imageName);

        imageRef.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        if (downloadUri != null) {
                                            String imageUrl = downloadUri.toString();
                                            userRef.child("profileImageUrl").setValue(imageUrl);
                                            userProfile_IMG_profileImageView.setImageURI(imageUri);
                                            Picasso.get().load(imageUri)
                                                    .transform(new CircleTransformation())
                                                    .into(userProfile_IMG_profileImageView);
                                            Toast.makeText(UserProfileActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(UserProfileActivity.this, "Failed to upload image to Firebase Storage", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void moveToHomePage() {
        Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
