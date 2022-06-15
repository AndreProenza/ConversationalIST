package pt.ulisboa.tecnico.conversationalist.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import pt.ulisboa.tecnico.conversationalist.R;
import pt.ulisboa.tecnico.conversationalist.adapter.RoomsAdapter;
import pt.ulisboa.tecnico.conversationalist.model.Room;

public class RoomsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton searchBtn;
    //Button loadRoomsBtn;
    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms;
    DatabaseReference db;
    RoomsAdapter roomsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RoomsActivity.this, MainActivity.class));
            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_rooms);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //************************************************
        //FIREBASE DATABASE
        db = FirebaseDatabase.getInstance().getReference("room");
        //************************************************

        rooms = new ArrayList<>();
        roomsAdapter = new RoomsAdapter(RoomsActivity.this, rooms);
        recyclerView.setAdapter(roomsAdapter);

        //************************************************
        //FIREBASE DATABASE

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    rooms.add(room);
                }
                roomsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //************************************************

        /**
        loadRoomsBtn = findViewById(R.id.load_rooms);
        loadRoomsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RoomsActivity.this, RoomListActivity.class));
                finish();
            }
        });
         */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RoomsActivity.this, MainActivity.class));
        finish();
    }

}