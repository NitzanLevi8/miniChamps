package Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minichamps_nitzanlevi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import Models.Child;
import Models.Gift;
import Models.TaskToDo;

public class EmptyChildAdapter extends RecyclerView.Adapter<EmptyChildAdapter.EmptyChildViewHolder> {

    private List<Child> childList;
    private DatabaseReference childRef;

    public EmptyChildAdapter(List<Child> childList) {
        this.childList = childList;
        this.childRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("children");
    }

    // Inner ViewHolder class
    public static class EmptyChildViewHolder extends RecyclerView.ViewHolder {
        TextView childName;
        TextView coinsCount;

        public EmptyChildViewHolder(@NonNull View itemView) {
            super(itemView);
            childName = itemView.findViewById(R.id.child_name);
            coinsCount = itemView.findViewById(R.id.coins_count);
        }
    }

    @NonNull
    @Override
    public EmptyChildAdapter.EmptyChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_child_view, parent, false);
        return new EmptyChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyChildAdapter.EmptyChildViewHolder holder, int position) {
        Child child = childList.get(position);
        holder.childName.setText(child.getName());
        holder.coinsCount.setText(String.valueOf(child.getCoinsBalance()));
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }


}