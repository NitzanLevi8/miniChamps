package com.example.minichamps_nitzanlevi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.TaskToDo;

public class SetNewTaskActivity extends AppCompatActivity {

    private EditText setTask_TXT_taskName;
    private EditText setTask_TXT_taskDescription;
    private EditText setTask_TXT_coins;
    private Spinner setTask_SPNR_day;
    private Spinner setTask_SPNR_hour;
    private Spinner setTask_SPNR_minute;
    private Spinner setTask_SPNR_child;
    private Button setTask_BTN_createTask;
    private ImageButton setTask_BTN_back;
    private DatabaseReference tasksRef;

    private String taskName;
    private String taskDescription;
    private String day;
    private int hour;
    private int minute;
    private String selectedChild;
    private int coins;

    private boolean isCreatingTask = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_task);
        tasksRef = FirebaseDatabase.getInstance().getReference().child("tasks");
        findViews();
        setUpSpinners();
        initViews();
    }

    private void findViews(){
        // Initialize views
        setTask_TXT_taskName = findViewById(R.id.setTask_TXT_taskName);
        setTask_TXT_taskDescription = findViewById(R.id.setTask_TXT_taskDescription);
        setTask_TXT_coins = findViewById(R.id.setTask_TXT_coins);
        setTask_SPNR_day = findViewById(R.id.setTask_SPNR_day);
        setTask_SPNR_hour = findViewById(R.id.setTask_SPNR_hour);
        setTask_SPNR_minute = findViewById(R.id.setTask_SPNR_minute);
        setTask_SPNR_child = findViewById(R.id.setTask_SPNR_child);
        setTask_BTN_createTask = findViewById(R.id.setTask_BTN_createTask);
        setTask_BTN_back = findViewById(R.id.setTask_BTN_back);
    }

    private void setUpSpinners() {
        setUpDaySpinner();
        setUpChildSpinner();
        setUpHourSpinner();
        setUpMinuteSpinner();
    }

    private void initViews(){
        setTask_BTN_createTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTask();
            }
        });

        setTask_BTN_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetNewTaskActivity.this, AllTasksActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setUpDaySpinner() {
        List<String> days = new ArrayList<>();
        days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setTask_SPNR_day.setAdapter(adapter);
    }

    private void setUpChildSpinner() {
        fetchChildNamesFromDatabase();
    }

    private void fetchChildNamesFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("children");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> childrenNames = new ArrayList<>();
                final Map<String, String> childrenMap = new HashMap<>(); // Map to store child names and IDs

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String childId = childSnapshot.getKey(); // Get the child ID
                    String childName = childSnapshot.child("name").getValue(String.class);
                    childrenNames.add(childName);
                    childrenMap.put(childName, childId); // Store child name and ID in the map
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(SetNewTaskActivity.this, android.R.layout.simple_spinner_item, childrenNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                setTask_SPNR_child.setAdapter(adapter);
                setTask_SPNR_child.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedChild = childrenMap.get(parent.getItemAtPosition(position).toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void setUpHourSpinner() {
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hours);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setTask_SPNR_hour.setAdapter(adapter);
    }

    private void setUpMinuteSpinner() {
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, minutes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setTask_SPNR_minute.setAdapter(adapter);
    }


    private void createTask() {
        if (isCreatingTask) {
            return;
        }
        isCreatingTask = true;
        setTask_BTN_createTask.setEnabled(false);
        getValuesFromSpinnersAndText();
        String taskId = tasksRef.push().getKey();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userTasksRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("tasks");
        TaskToDo task = new TaskToDo(taskId, selectedChild, taskName, taskDescription, day, hour, minute, coins, false);
        userTasksRef.child(taskId).setValue(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SetNewTaskActivity.this, "Task created: " + taskName, Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetNewTaskActivity.this, "Failed to create task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setTask_BTN_createTask.setEnabled(true);
                        isCreatingTask = false;
                    }
                });
    }


    private void getValuesFromSpinnersAndText() {
        taskName = setTask_TXT_taskName.getText().toString();
        taskDescription = setTask_TXT_taskDescription.getText().toString();
        day = setTask_SPNR_day.getSelectedItem().toString();

        String hourString = setTask_SPNR_hour.getSelectedItem().toString(); // This might be the problematic line
        String minuteString = setTask_SPNR_minute.getSelectedItem().toString();
        String coinsString = setTask_TXT_coins.getText().toString();

        if (!hourString.isEmpty()) {
            hour = Integer.parseInt(hourString);
        } else {
            Toast.makeText(this, "Please select hour", Toast.LENGTH_SHORT).show();
            hour = 0;
        }

        if (!minuteString.isEmpty()) {
            minute = Integer.parseInt(minuteString);
        } else {
            Toast.makeText(this, "Please select minute", Toast.LENGTH_SHORT).show();
            minute = 0;
        }
        if (!coinsString.isEmpty()) {
            coins = Integer.parseInt(coinsString);
        } else {
            Toast.makeText(this, "Please enter coins", Toast.LENGTH_SHORT).show();
            coins = 0;
        }
        getChildIdFromName(setTask_SPNR_child.getSelectedItem().toString());
    }


    private void getChildIdFromName(String childName) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("children");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String name = childSnapshot.child("name").getValue(String.class);
                    if (name != null && name.equals(childName)) {
                        selectedChild = childSnapshot.getKey();
                        createTask();
                        return;
                    }
                }
                Toast.makeText(SetNewTaskActivity.this, "Failed to find child ID for " + childName, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void clearFields() {
        setTask_TXT_taskName.getText().clear();
        setTask_TXT_taskDescription.getText().clear();
        setTask_TXT_coins.getText().clear();
        setTask_SPNR_day.setSelection(0);
        setTask_SPNR_hour.setSelection(0);
        setTask_SPNR_minute.setSelection(0);
        setTask_SPNR_child.setSelection(0);
    }

}
