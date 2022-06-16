package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.ChatActivity;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.RoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.recyclerview.RecyclerViewAddRoomsInterface;

public class RoomsActivity extends AppCompatActivity implements RecyclerViewAddRoomsInterface {

    private ImageButton btnBack;
    private ImageButton searchBtn;
    //Button loadRoomsBtn;
    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms;
    DatabaseReference db;
    RoomsAdapter roomsAdapter;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        queue = Volley.newRequestQueue(RoomsActivity.this);

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
        //db = FirebaseDatabase.getInstance().getReference("room");
        //************************************************

        fetchRooms();

        //TODO call to save channel in db
        //FeedReaderDbHelper.getInstance(getApplicationContext()).createChannel("628e1fa903146c7d0cc43b23","b");


        //************************************************
        //FIREBASE DATABASE

        /*
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
        });*/

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

    private void fetchRooms() {
        rooms = new ArrayList<>();

        String url = "https://cmuapi.herokuapp.com/api/rooms";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomsActivity.this, "Rooms received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        Room room = new Room(jresponse.getString("name"), jresponse.getString("id"));
                        if(FeedReaderDbHelper.getInstance(getApplicationContext()).isChannelSubscribed(jresponse.getString("id"))) {
                            System.out.println("Name: " + jresponse.getString("name") + "; ID: " + jresponse.getString("id") +"\n");
                            rooms.add(room);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                roomsAdapter = new RoomsAdapter(RoomsActivity.this, rooms, RoomsActivity.this);
                recyclerView.setAdapter(roomsAdapter);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomsActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RoomsActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onItemClick(int position) {

    }
}