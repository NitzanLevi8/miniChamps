package Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minichamps_nitzanlevi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import Models.TaskToDo;

public class EmptyTaskAdapter extends RecyclerView.Adapter<EmptyTaskAdapter.EmptyTaskViewHolder> {

    private List<TaskToDo> tasks;
    private DatabaseReference tasksRef;

    public EmptyTaskAdapter(List<TaskToDo> tasks) {
        this.tasks = tasks;
        this.tasksRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks");
    }

    @NonNull
    @Override
    public EmptyTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.empty_task_view, parent, false);
        return new EmptyTaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyTaskViewHolder holder, int position) {
        TaskToDo task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class EmptyTaskViewHolder extends RecyclerView.ViewHolder {
        private TextView emptyTaskView_TXT_taskName;
        private TextView emptyTaskView_TXT_taskDescription;
        private TextView emptyTaskView_TXT_taskCoinsVal;
        private TextView emptyTaskView_TXT_childName;
        private ImageButton emptyTaskView_BTN_deleteTask;
        private ImageButton emptyTaskView_BTN_taskIsDone;


        public EmptyTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyTaskView_TXT_taskName = itemView.findViewById(R.id.emptyTaskView_TXT_taskName);
            emptyTaskView_TXT_taskDescription = itemView.findViewById(R.id.emptyTaskView_TXT_taskDescription);
            emptyTaskView_TXT_taskCoinsVal = itemView.findViewById(R.id.emptyTaskView_TXT_taskCoinsVal);
            emptyTaskView_BTN_deleteTask = itemView.findViewById(R.id.emptyTaskView_BTN_deleteTask);
            emptyTaskView_BTN_taskIsDone = itemView.findViewById(R.id.emptyTaskView_BTN_taskIsDone);
            emptyTaskView_TXT_childName = itemView.findViewById(R.id.emptyTaskView_TXT_childName);
        }

        public void bind(TaskToDo task) {
            emptyTaskView_TXT_taskName.setText(task.getName());
            emptyTaskView_TXT_taskDescription.setText(task.getDescription());
            emptyTaskView_TXT_taskCoinsVal.setText("Coins: " + task.getCoinsRewarded());
            getChildNameFromId(task.getChildId());
            emptyTaskView_BTN_deleteTask.setOnClickListener(v -> deleteTask(task));
            emptyTaskView_BTN_taskIsDone.setOnClickListener(v -> markTaskAsDone(task));
        }

        private void deleteTask(TaskToDo task) {
            tasksRef.child(task.getId()).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error != null) {
                        Log.e("EmptyTaskAdapter", "Failed to delete task: " + error.getMessage());
                    }
                }
            });
        }

        private void getChildNameFromId(String childId) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUserId)
                    .child("children")
                    .child(childId);

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String childName = dataSnapshot.child("name").getValue(String.class);
                        if (childName != null) {
                            emptyTaskView_TXT_childName.setText(childName);
                            return;
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        private void markTaskAsDone(TaskToDo task) {
            updateChildCoins(task.getChildId(), task.getCoinsRewarded());
            deleteTask(task);
        }


        private void updateChildCoins(String childId, int coinsToAdd) {
            DatabaseReference childRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("children")
                    .child(childId);

            childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Integer currentCoins = dataSnapshot.child("coinsBalance").getValue(Integer.class);
                        if (currentCoins != null) {
                            int newCoinsValue = currentCoins + coinsToAdd;
                            childRef.child("coinsBalance").setValue(newCoinsValue, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError error, DatabaseReference ref) {
                                    if (error != null) {
                                        Log.e("EmptyTaskAdapter", "Failed to update coins balance: " + error.getMessage());
                                    } else {
                                        Log.d("EmptyTaskAdapter", "Coins balance updated successfully");
                                    }
                                }
                            });
                        } else {
                            Log.e("EmptyTaskAdapter", "Current coins balance is null for child");
                        }
                    } else {
                        Log.e("EmptyTaskAdapter", "Child does not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("EmptyTaskAdapter", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }
}
