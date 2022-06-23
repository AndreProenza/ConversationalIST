package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;


import static android.content.ContentValues.TAG;

import static android.content.Context.MODE_PRIVATE;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.RoomType;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.MainRoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.RoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;
import pt.ulisboa.tecnico.cmov.conversational_ist.firebase.FirebaseHandler;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewEnterChatInterface;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.User;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.profiles.MyProfileActivity;

public class MainActivity extends AppCompatActivity implements RecyclerViewEnterChatInterface {

    private static final int REQUEST_LOCATION_CODE = 501;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private LinearLayout btnLogOut;
    private CircularImageView profileImage;
    private TextView userName;

    //private FirebaseAuth mAuth;
    private String userId;

    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms = new ArrayList<>();
    MainRoomsAdapter roomsAdapter;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        //isUserLoggedIn();
        initUser();
        init();
        initProfile();

        String appLinkAction = getIntent().getAction();
        Uri appLinkData = getIntent().getData();
        if (appLinkData != null) {
            String roomID = appLinkData.getLastPathSegment();
            fetchRoomAndCreate(roomID);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_CODE);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_rooms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getRoomsSubscribed();

        // ATTENTION: This was auto-generated to handle app links.

        // ATTENTION: This was auto-generated to handle app links.
    }

    private void fetchRoomAndCreate(String roomID) {

        String url = "https://cmuapi.herokuapp.com/api/rooms/room?roomID=" + roomID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
                            );
                    FeedReaderDbHelper.getInstance(getApplicationContext()).createChannel(r);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);
    }

    private void getRoomsSubscribed() {
        rooms = FeedReaderDbHelper.getInstance(getApplicationContext()).getAllChannels();
        roomsAdapter = new MainRoomsAdapter(MainActivity.this, rooms, this);
        recyclerView.setAdapter(roomsAdapter);
    }

    private void initUser() {
        SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
        userId = sh.getString("saved_username","");
        Log.d("UserId: ", userId);
    }

    private void initProfile() {
        userName = findViewById(R.id.name);
        profileImage = findViewById(R.id.profile);

        //String userId = mAuth.getUid().toString();
        FirebaseHandler.getCurrentProfileInfoMain(userId, userName, profileImage);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            }
        });
    }

    /**
    private void isUserLoggedIn() {
        btnLogOut = findViewById(R.id.btnLogOut);
        mAuth = FirebaseAuth.getInstance();

        btnLogOut.setOnClickListener(view ->{
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }
     */

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout =  findViewById(R.id.drawer_layout);


        //Set Actionbar
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });


        //Drawer
        final NavigationView nav_view =  findViewById(R.id.nav_layout);
        ActionBarDrawerToggle toggle =  new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) { super.onDrawerOpened(drawerView); }
        };

        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //TODO Later
                drawerLayout.closeDrawers();
                return true;
            }
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
            startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            drawerLayout.closeDrawers();
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
            deleteAccount();
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            drawerLayout.closeDrawers();
        });

        /** Logout
        nav_view.findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            drawerLayout.closeDrawers();
        });
         */
    }


    @Override
    protected void onStart() {
        super.onStart();

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_rooms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getRoomsSubscribed();

        /** FIREBASE AUTH
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
         */
    }


    private void deleteAccount() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("saved_username");
        editor.clear();
        editor.apply();
        //Delete Firebase Account
        FirebaseHandler.deleteUserId(userId);
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
        TextView tvRoomName = (TextView) cardView.findViewById(R.id.room_name);
        String roomName = tvRoomName.getText().toString();
        Log.d("RoomName: ", roomName);
        TextView tvRoomId = (TextView) cardView.findViewById(R.id.room_id);
        String roomId = tvRoomId.getText().toString();
        Log.d("RoomName: ", roomId);
        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
        intent.putExtra("roomName", roomName);
        intent.putExtra("roomId", roomId);
        intent.putExtra("username", userId);
        startActivity(intent);
    }
}