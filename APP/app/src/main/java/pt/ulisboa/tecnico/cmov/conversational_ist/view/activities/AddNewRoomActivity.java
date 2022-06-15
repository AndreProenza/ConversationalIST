package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pt.ulisboa.tecnico.cmov.conversational_ist.MainActivity;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class AddNewRoomActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText edRoomName, edRoomDescription;
    private Spinner spinner;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_room);

        init();
        initSpinner();
        initClick();
    }

    private void init() {
        edRoomName = findViewById(R.id.ed_room_name);
        edRoomDescription = findViewById(R.id.ed_room_description);
        spinner = findViewById(R.id.sp_new_room);
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.room_visibility, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void initClick() {
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check Validate
                if (TextUtils.isEmpty(edRoomName.getText().toString())) {
                    edRoomName.setError("Field required!");
                } else if (TextUtils.isEmpty(edRoomDescription.getText().toString())) {
                    edRoomDescription.setError("Field required!");
                } else if (spinner.getSelectedItem().toString() == null) {
                    Toast.makeText(getApplicationContext(), "Please Select Room visibility", Toast.LENGTH_SHORT).show();
                }
                else {
                    addRoom();
                }
            }
        });
    }

    private void addRoom() {
        String roomName = edRoomName.getText().toString();
        String roomDescription = edRoomDescription.getText().toString();
        String roomVisibility = spinner.getSelectedItem().toString();

        Room room = new Room(roomName, roomDescription, roomVisibility);

        //*********************
        //FIREBASE DATABASE
        db = FirebaseDatabase.getInstance().getReference("room");
        String roomId = db.push().getKey();
        db.child(roomId).setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddNewRoomActivity.this, "Room created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddNewRoomActivity.this, MainActivity.class));
                }
                else {
                    Toast.makeText(AddNewRoomActivity.this, "Room not created\nTry again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //*********************

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Your Room is " + text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}