package Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minichamps_nitzanlevi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Models.Gift;

public class EmptyGiftAdapter extends RecyclerView.Adapter<EmptyGiftAdapter.EmptyGiftViewHolder> {

    private List<Gift> gifts;

    public EmptyGiftAdapter(List<Gift> gifts) {
        this.gifts = gifts;
    }

    @NonNull
    @Override
    public EmptyGiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.empty_gift_view, parent, false);
        return new EmptyGiftViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyGiftViewHolder holder, int position) {
        // Bind data to views
        Gift gift = gifts.get(position);
        holder.bind(gift, gifts, this, holder.itemView.getContext());
    }

    @Override
    public int getItemCount() {
        return gifts.size();
    }

    public class EmptyGiftViewHolder extends RecyclerView.ViewHolder {
        private TextView emptyGiftView_TXT_giftName;
        private TextView emptyGiftView_TXT_giftDescription;
        private TextView emptyGiftView_TXT_giftCoinsVal;
        private ImageButton emptyGiftView_BTN_deleteGift;
        private ImageButton emptyGiftView_BTN_giveGift;

        public EmptyGiftViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyGiftView_TXT_giftName = itemView.findViewById(R.id.emptyGiftView_TXT_giftName);
            emptyGiftView_TXT_giftDescription = itemView.findViewById(R.id.emptyGiftView_TXT_giftDescription);
            emptyGiftView_TXT_giftCoinsVal = itemView.findViewById(R.id.emptyGiftView_TXT_giftCoinsVal);
            emptyGiftView_BTN_deleteGift = itemView.findViewById(R.id.emptyGiftView_BTN_deleteGift);
            emptyGiftView_BTN_giveGift = itemView.findViewById(R.id.emptyGiftView_BTN_giveGift);
        }

        public void bind(Gift gift, List<Gift> gifts, EmptyGiftAdapter adapter, Context context) {
            emptyGiftView_TXT_giftName.setText(gift.getName());
            emptyGiftView_TXT_giftDescription.setText(gift.getDescription());
            emptyGiftView_TXT_giftCoinsVal.setText("Coins: " + gift.getCoinsValue());
            emptyGiftView_BTN_deleteGift.setOnClickListener(v -> {
                deleteGift(gift.getId(), gifts, adapter);
            });
            emptyGiftView_BTN_giveGift.setOnClickListener(v -> {
                showSelectChildDialog(context, gift);
            });
        }

        private void deleteGift(String giftId, List<Gift> gifts, EmptyGiftAdapter adapter) {
            DatabaseReference giftRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("gifts").child(giftId);
            giftRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EmptyGiftAdapter", "Gift deleted successfully from database");
                        // Remove the gift from the local list
                        for (int i = 0; i < gifts.size(); i++) {
                            if (gifts.get(i).getId().equals(giftId)) {
                                gifts.remove(i);
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("EmptyGiftAdapter", "Error deleting gift from database: " + e.getMessage()));
        }

        private void getChildCoinsFromName(String childName, ChildCoinsListener listener) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("children")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String childId = null;
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                String name = childSnapshot.child("name").getValue(String.class);
                                if (name != null && name.equals(childName)) {
                                    // Found matching child name, get the child ID
                                    childId = childSnapshot.getKey();
                                    break;
                                }
                            }

                            if (childId != null) {
                                DatabaseReference childRef = usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("children").child(childId).child("coinsBalance");
                                childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Long coins = snapshot.getValue(Long.class);
                                        if (coins != null) {
                                            listener.onChildCoinsReceived(coins);
                                        } else {
                                            listener.onChildCoinsError(new Exception("Child's coins not found"));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        listener.onChildCoinsError(error.toException());
                                    }
                                });
                            } else {
                                listener.onChildCoinsError(new Exception("Child with name " + childName + " not found."));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            listener.onChildCoinsError(databaseError.toException());
                        }
                    });
        }

        private void checkIfChildHasEnoughCoins(String childName, Gift gift, Context context) {
            getChildCoinsFromName(childName, new ChildCoinsListener() {
                @Override
                public void onChildCoinsReceived(long childCoins) {
                    long giftCoinsValue = gift.getCoinsValue();
                    if (childCoins >= giftCoinsValue) {
                        Toast.makeText(context, childName + " has enough coins to buy the gift", Toast.LENGTH_SHORT).show();
                        getChildIdAndPurchase(childName, childCoins, giftCoinsValue, gift, context);
                    } else {
                        Toast.makeText(context, childName + " does not have enough coins to buy the gift", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onChildCoinsError(Exception e) {
                    Toast.makeText(context, "Failed to get coins balance for " + childName + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void getChildIdAndPurchase(String childName, long childCoins, long giftCoinsValue, Gift gift, Context context) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("children")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String childId = null;
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                String name = childSnapshot.child("name").getValue(String.class);
                                if (name != null && name.equals(childName)) {
                                    childId = childSnapshot.getKey();
                                    break;
                                }
                            }

                            if (childId != null) {
                                updateChildCoinsBalance(childId, childCoins, giftCoinsValue);
                            } else {
                                Toast.makeText(context, "Child with name " + childName + " not found.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(context, "Failed to get child ID for " + childName + ": " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        private void updateChildCoinsBalance(String childId, long childCoins, long giftCoinsValue) {
            long newBalance = childCoins - giftCoinsValue;

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
            DatabaseReference childCoinsRef = usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("children").child(childId).child("coinsBalance");
            childCoinsRef.setValue(newBalance)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EmptyGiftAdapter", "Child's coins balance updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EmptyGiftAdapter", "Failed to update child's coins balance: " + e.getMessage());
                    });
        }



        private void showSelectChildDialog(Context context, Gift gift) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_child, null);
            Spinner spinnerChildren = dialogView.findViewById(R.id.spinner_children);

            fetchChildNamesFromDatabase(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> children = new ArrayList<>();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String childName = childSnapshot.child("name").getValue(String.class);
                        if (childName != null) {
                            children.add(childName);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, children);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerChildren.setAdapter(adapter);

                    builder.setView(dialogView)
                            .setTitle("Select Child")
                            .setPositiveButton("OK", (dialog, which) -> {
                                String selectedChildName = spinnerChildren.getSelectedItem().toString();
                                checkIfChildHasEnoughCoins(selectedChildName, gift, context);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("EmptyGiftAdapter", "Failed to fetch child names: " + databaseError.getMessage());
                    Toast.makeText(context, "Failed to fetch child names", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void fetchChildNamesFromDatabase(ValueEventListener listener) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("children");
            databaseRef.addListenerForSingleValueEvent(listener);
        }
    }

    // Listener interface for child coins retrieval
    interface ChildCoinsListener {
        void onChildCoinsReceived(long childCoins);
        void onChildCoinsError(Exception e);
    }
}
