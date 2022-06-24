package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import static com.android.volley.toolbox.Volley.newRequestQueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonArrayRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


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

    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms;
    RoomsAdapter roomsAdapter;
    private FusedLocationProviderClient locationProvider;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        RequestQueue queue = newRequestQueue(RoomsActivity.this);

        SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
        userID = sh.getString("saved_userid","");

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(RoomsActivity.this, MainActivity.class));
            finish();
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_rooms);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        locationProvider = LocationServices.getFusedLocationProviderClient(this);

        getLocationAndFetchRooms();
        initSearch();
    }

    private void initSearch() {
        SearchView searchView = findViewById(R.id.search_view);
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
        if (rooms == null) { return;}
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
        locationProvider.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                fetchRooms(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
            }
            else {
                fetchRooms("","");
            }
        });
    }

    private void fetchRooms(String lat, String lng) {
        rooms = new ArrayList<>();

        String url = "http://cmuapi.herokuapp.com/api/rooms?lat=" + lat + "&lng=" + lng;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            Toast.makeText(RoomsActivity.this, "Rooms received!", Toast.LENGTH_SHORT).show();
            System.out.println(response);

            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject jresponse = response.getJSONObject(i);
                    boolean isGeoFenced = Integer.parseInt(jresponse.getString("roomType")) == RoomType.GEOFENCED.ordinal();
                    Room room = new Room(jresponse.getString("id"), jresponse.getString("name"), isGeoFenced, Double.parseDouble(jresponse.getString("lat")), Double.parseDouble(jresponse.getString("lng")), Integer.parseInt(jresponse.getString("radius")),0);
                    if(FeedReaderDbHelper.getInstance(getApplicationContext()).isChannelSubscribed(jresponse.getString("id"))) {
                        System.out.println("Name: " + jresponse.getString("name") + "; ID: " + jresponse.getString("id") +"\n");
                        rooms.add(room);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            roomsAdapter = new RoomsAdapter(RoomsActivity.this, rooms, RoomsActivity.this,userID);
            recyclerView.setAdapter(roomsAdapter);
        }, error -> Toast.makeText(RoomsActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show());

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

    }
}