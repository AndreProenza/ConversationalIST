package pt.ulisboa.tecnico.cmov.conversational_ist.firebase;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import pt.ulisboa.tecnico.cmov.conversational_ist.model.User;

public class FirebaseHandler {

    private static final String TAG = "FirebaseHandler";
    private static DatabaseReference db;

    public static void registerUser(DatabaseReference db, String userId, String userName) {
        User user = new User();
        user.setUsername(userName);

        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).setValue(user);
    }

    public static void getCurrentProfileInfo(String userId, TextView userName, TextView userName2, CircularImageView profileImage, EditText bio) {
        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("username")) {
                    userName.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());
                    userName2.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());
                }
                if (snapshot.hasChild("bio")) {
                    bio.setText(Objects.requireNonNull(snapshot.child("bio").getValue()).toString());
                }
                if (snapshot.hasChild("photo")) {
                    String photo = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();
                    Picasso.get().load(photo).into(profileImage);
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public static void getCurrentProfileInfoMain(String userId, TextView userName, CircularImageView profileImage) {
        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("username")) {
                    userName.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());
                }
                if (snapshot.hasChild("photo")) {
                    String photo = snapshot.child("photo").getValue().toString();
                    Picasso.get().load(photo).into(profileImage);
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public static void uploadUserBio(String userId, String bioText) {
        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).child("bio").setValue(bioText);
    }

    public static void deleteUserId(String userId) {
        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).removeValue();
    }
}
