package pt.ulisboa.tecnico.cmov.conversational_ist.firebase;

import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import pt.ulisboa.tecnico.cmov.conversational_ist.model.User;

public class FirebaseHandler {

    private static String TAG = "FirebaseHandler";
    private static DatabaseReference db;

    public static void registerUser(DatabaseReference db, String userId, String userName) {
        User user = new User();
        user.setUsername(userName);
        user.setBio(null);
        user.setPhoto(null);

        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).setValue(user);
    }

    public static void getCurrentProfileInfo(String userId, TextView userName, CircularImageView profileImage) {
        db = FirebaseDatabase.getInstance().getReference("users");
        db.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                userName.setText(snapshot.child("username").getValue().toString());
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
}
