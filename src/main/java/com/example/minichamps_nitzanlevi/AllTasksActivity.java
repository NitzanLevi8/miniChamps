package com.example.minichamps_nitzanlevi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

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

import java.util.ArrayList;
import java.util.List;

import Adapters.EmptyTaskAdapter;
import Models.TaskToDo;

public class AllTasksActivity extends AppCompatActivity {

    private RecyclerView allTasks_RCLV_tasks;
    private ImageButton allTasks_BTN_newTask;
    private ImageButton allTasks_BTN_homepage;
    private List<TaskToDo> taskList;
    private EmptyTaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);

        findViews();
        initViews();
        fetchTasksFromDatabase();
    }

    private void findViews() {
        allTasks_RCLV_tasks = findViewById(R.id.allTasks_RCLV_tasks);
        allTasks_BTN_newTask = findViewById(R.id.allTasks_BTN_newTask);
        allTasks_BTN_homepage = findViewById(R.id.allTasks_BTN_homePage);
    }

    private void initViews() {
        allTasks_BTN_homepage.setOnClickListener(v -> {
            Intent intent = new Intent(AllTasksActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        allTasks_BTN_newTask.setOnClickListener(v -> {
            Intent intent = new Intent(AllTasksActivity.this, SetNewTaskActivity.class);
            startActivity(intent);
            finish();
        });

        allTasks_RCLV_tasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new EmptyTaskAdapter(taskList);
        allTasks_RCLV_tasks.setAdapter(taskAdapter);
    }

    private void fetchTasksFromDatabase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("tasks");
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    taskList.clear();
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        TaskToDo task = taskSnapshot.getValue(TaskToDo.class);
                        if (task != null) {
                            taskList.add(task);
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("AllTasksActivity", "Failed to fetch tasks: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("AllTasksActivity", "Current user is null");
        }
    }
}
