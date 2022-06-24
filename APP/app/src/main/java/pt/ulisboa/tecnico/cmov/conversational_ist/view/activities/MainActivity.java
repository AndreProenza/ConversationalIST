package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.RoomType;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.MainRoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;
import pt.ulisboa.tecnico.cmov.conversational_ist.firebase.FirebaseHandler;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewEnterChatInterface;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.profiles.MyProfileActivity;

public class MainActivity extends AppCompatActivity implements RecyclerViewEnterChatInterface {

    private DrawerLayout drawerLayout;
    private LinearLayout initialLayout;

    private String username;

    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms = new ArrayList<>();
    MainRoomsAdapter roomsAdapter;

    SharedPreferences sharedPref;
    SharedPreferences sharedPrefMode;

    private Dialog dialog;


    private final BroadcastReceiver Updated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if(intent.getAction().equals("messages_read")) {
                for (int i = 0; i != rooms.size(); i++) {
                    if (rooms.get(i).getRoomId().equals(extras.get("roomID"))) {
                        rooms.get(i).setUnreadNum(0);
                        roomsAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            } else {
                if (extras != null) {
                    Message m = (Message) extras.get("message");
                    for (int i = 0; i != rooms.size(); i++) {
                        if (rooms.get(i).getRoomId().equals(m.getRoomID())) {
                            rooms.get(i).setUnreadNum(rooms.get(i).getUnreadNum() + 1);
                            roomsAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPrefMode = getSharedPreferences("mode", Context.MODE_PRIVATE);

        initUser();
        init();
        initProfile();
        initDialog();

        String appLinkAction = getIntent().getAction();
        Uri appLinkData = getIntent().getData();
        if (appLinkData != null) {
            String roomID = appLinkData.getLastPathSegment();
            fetchRoomAndCreate(roomID);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    44
            );
        }

        recyclerView = findViewById(R.id.recycle_view_rooms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        registerReceiver(Updated, new IntentFilter("message_inserted"));
        registerReceiver(Updated, new IntentFilter("messages_read"));
    }

    private void fetchRoomAndCreate(String roomID) {

        String url = "https://cmuapi.herokuapp.com/api/rooms/room?roomID=" + roomID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Toast.makeText(MainActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
            System.out.println(response);

            try {
                boolean isGeoFenced = response.getInt("roomType") == RoomType.GEOFENCED.ordinal();
                Room r = new Room(response.getString("id"),
                        response.getString("name"),
                        isGeoFenced,
                        response.getDouble("lat"),
                        response.getDouble("lng"),
                        response.getInt("radius")
                        ,0);
                FeedReaderDbHelper.getInstance(getApplicationContext()).createChannel(r);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Toast.makeText(MainActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);
    }

    private void getRoomsSubscribed() {
        rooms = FeedReaderDbHelper.getInstance(getApplicationContext()).getGeoFencedRooms(0);
        System.out.println(rooms);
        initialLayout = findViewById(R.id.initial_layout);
        initAdapter();
        filterGeoRooms();
    }

    private void initAdapter(){
        if (rooms.isEmpty()) {
            initialLayout.setVisibility(View.VISIBLE);
        }
        else {
            initialLayout.setVisibility(View.GONE);
        }
        roomsAdapter = new MainRoomsAdapter(MainActivity.this, rooms, this);
        recyclerView.setAdapter(roomsAdapter);

    }

    @SuppressLint("MissingPermission")
    private void filterGeoRooms() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient locationProvider = LocationServices.getFusedLocationProviderClient(this);
            locationProvider.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    int size = rooms.size();
                    //ArrayList<Room> room_list = new ArrayList<>();
                    ArrayList<Room> geoRooms = FeedReaderDbHelper.getInstance(getApplicationContext()).getGeoFencedRooms(1);

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    for (Room r : geoRooms) {
                        float[] result = new float[1];
                        Location.distanceBetween(latitude, longitude, r.getLat(), r.getLng(), result);
                        System.out.println("Location distance : " + result[0] / 1000);
                        if ((result[0] / 1000) < r.getRadius()) {
                            rooms.add(r);
                        }
                    }
                    roomsAdapter.notifyItemRangeChanged(size-1, rooms.size());
                }
                if (!rooms.isEmpty()) {
                    initialLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    private void initUser() {
        SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
        username = sh.getString("saved_username","");
        Log.d("Username: ", username);
    }

    private void initProfile() {
        TextView userName = findViewById(R.id.name);
        userName.setText(username);
        CircularImageView profileImage = findViewById(R.id.profile);

        FirebaseHandler.getCurrentProfileInfoMain(username, userName, profileImage);

        profileImage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyProfileActivity.class)));
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout =  findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());


        //Drawer
        final NavigationView nav_view =  findViewById(R.id.nav_layout);
        ActionBarDrawerToggle toggle =  new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) { super.onDrawerOpened(drawerView); }
        };

        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(item -> {

            //TODO Later
            drawerLayout.closeDrawers();
            return true;
        });

        // initClick
        nav_view.findViewById(R.id.btn_groups).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(MainActivity.this, RoomsActivity.class));
            finish();
        });

        nav_view.findViewById(R.id.ln_new_group).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(MainActivity.this, AddNewRoomActivity.class));
            finish();
        });

        nav_view.findViewById(R.id.settings_btn).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            finish();
        });

        nav_view.findViewById(R.id.recommendations_btn).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecommendationsActivity.class));
            drawerLayout.closeDrawers();
        });

        nav_view.findViewById(R.id.ll_policy).setOnClickListener(v -> {
            initPolicy();
            drawerLayout.closeDrawers();
        });

        nav_view.findViewById(R.id.btnDelAccount).setOnClickListener(v -> {
            dialog.show();
            drawerLayout.closeDrawers();
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        recyclerView = findViewById(R.id.recycle_view_rooms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getRoomsSubscribed();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initDialog() {
        //Create the Dialog here
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.delete_account_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.delete_account_background));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false); //Optional
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button yesBtn = dialog.findViewById(R.id.btn_yes);
        Button noBtn = dialog.findViewById(R.id.btn_no);

        yesBtn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            deleteAccount();
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        });

        noBtn.setOnClickListener(v -> dialog.dismiss());
    }


    private void deleteAccount() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("saved_username");
        editor.clear();
        editor.apply();
        //Erase Darkmode
        SharedPreferences.Editor editorMode = sharedPrefMode.edit();
        editorMode.remove("mode");
        editorMode.clear();
        editorMode.apply();
        //Delete Firebase Account
        FirebaseHandler.deleteUserId(username);
        //Delete tables
        FeedReaderDbHelper.getInstance(MainActivity.this).getWritableDatabase().execSQL(FeedReaderDbHelper.SQL_DELETE_ENTRIES_CHANNELS);
        FeedReaderDbHelper.getInstance(MainActivity.this).getWritableDatabase().execSQL(FeedReaderDbHelper.SQL_DELETE_ENTRIES_MESSAGES);
        //Create Tables
        FeedReaderDbHelper.getInstance(MainActivity.this).getWritableDatabase().execSQL(FeedReaderDbHelper.SQL_CREATE_CHANNELS);
        FeedReaderDbHelper.getInstance(MainActivity.this).getWritableDatabase().execSQL(FeedReaderDbHelper.SQL_CREATE_MESSAGES);
    }

    private void initPolicy() {
        Uri uri = Uri.parse("https://github.com/AndreProenza/ConversationalIST");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        CardView cardView = (CardView) recyclerView.getChildAt(position);
        TextView tvRoomName = cardView.findViewById(R.id.room_name);
        String roomName = tvRoomName.getText().toString();
        Log.d("RoomName: ", roomName);
        TextView tvRoomId = cardView.findViewById(R.id.room_id);
        String roomId = tvRoomId.getText().toString();
        Log.d("RoomName: ", roomId);
        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
        intent.putExtra("roomName", roomName);
        intent.putExtra("roomId", roomId);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("rooms", new ArrayList<>(roomsAdapter.getList()));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getParcelableArrayList("rooms");
    }
}