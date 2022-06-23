package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.RoomType;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.RoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewAddRoomsInterface;

public class RoomsActivity extends AppCompatActivity implements RecyclerViewAddRoomsInterface {

    private ImageButton btnBack;
    private ImageButton searchBtn;
    //Button loadRoomsBtn;
    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms;
    DatabaseReference db;
    RoomsAdapter roomsAdapter;
    private RequestQueue queue;
    private SearchView searchView;
    private FusedLocationProviderClient locationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        queue = Volley.newRequestQueue(RoomsActivity.this);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        locationProvider = LocationServices.getFusedLocationProviderClient(this);

        getLocationAndFetchRooms();
        initSearch();

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
        @Override public void onClick(View v) {
        startActivity(new Intent(RoomsActivity.this, RoomListActivity.class));
        finish();
        }
        });
         */

    }

    private void initSearch() {
        searchView = findViewById(R.id.search_view);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRooms(newText);
                return true;
            }
        });
    }

    private void filterRooms(String text) {
        ArrayList<Room> filteredRooms = new ArrayList<>();
        for (Room r : rooms) {
            if (r.getRoomName().toLowerCase().contains(text.toLowerCase())) {
                filteredRooms.add(r);
            }
        }
        if (filteredRooms.isEmpty()) {
            Toast.makeText(this, "No room found", Toast.LENGTH_SHORT).show();
        } else {
            roomsAdapter.setFilteredRooms(filteredRooms);
        }
    }

    private void getLocationAndFetchRooms(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    44
            );
            fetchRooms("","");
        } else {
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationProvider.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    fetchRooms(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
                }
                else {
                    fetchRooms("","");
                }
            }
        });
    }

    private void fetchRooms(String lat, String lng) {
        rooms = new ArrayList<>();

        String url = "http://cmuapi.herokuapp.com/api/rooms?lat=" + lat + "&lng=" + lng;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomsActivity.this, "Rooms received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        boolean isGeoFenced = Integer.parseInt(jresponse.getString("roomType")) == RoomType.GEOFENCED.ordinal();
                        Room room = new Room(jresponse.getString("id"), jresponse.getString("name"), isGeoFenced, Double.parseDouble(jresponse.getString("lat")), Double.parseDouble(jresponse.getString("lng")), Integer.parseInt(jresponse.getString("radius")));
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

        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RoomsActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onItemClick(int position) {
        finish();
        startActivity(new Intent(RoomsActivity.this, MainActivity.class));
    }
}