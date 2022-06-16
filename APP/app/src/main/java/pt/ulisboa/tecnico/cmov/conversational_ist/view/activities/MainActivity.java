package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.MainRoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.RoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.firebase.FirebaseHandler;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewEnterChatInterface;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.User;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.profiles.MyProfileActivity;

public class MainActivity extends AppCompatActivity implements RecyclerViewEnterChatInterface {

    private ActionBar actionBar;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private LinearLayout btnLogOut;
    private CircularImageView profileImage;
    private TextView userName;

    private FirebaseAuth mAuth;

    RecyclerView recyclerView = null;
    private ArrayList<Room> rooms = new ArrayList<>();
    MainRoomsAdapter roomsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO : use this to enter in new room with ID
        FirebaseMessaging.getInstance().subscribeToTopic("628e1fa903146c7d0cc43b23").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Subscribe successful");
            }
        });

        isUserLoggedIn();
        init();
        initProfile();

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_rooms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getRoomsSubscribed();
    }

    private void getRoomsSubscribed() {
        rooms = FeedReaderDbHelper.getInstance(getApplicationContext()).getAllChannels();
        roomsAdapter = new MainRoomsAdapter(MainActivity.this, rooms, this);
        recyclerView.setAdapter(roomsAdapter);
        Toast.makeText(MainActivity.this, "Rooms received" , Toast.LENGTH_SHORT).show();
    }

    private void initProfile() {
        userName = findViewById(R.id.name);
        profileImage = findViewById(R.id.profile);

        String userId = mAuth.getUid().toString();
        FirebaseHandler.getCurrentProfileInfo(userId, userName, profileImage);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            }
        });
    }


    private void isUserLoggedIn() {
        btnLogOut = findViewById(R.id.btnLogOut);
        mAuth = FirebaseAuth.getInstance();

        btnLogOut.setOnClickListener(view ->{
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout =  findViewById(R.id.drawer_layout);
        //btnGroups = findViewById(R.id.btn_groups);

        //Set Actionbar
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

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
            startActivity(new Intent(MainActivity.this, RoomsActivity.class));
            drawerLayout.closeDrawers();
        });

        nav_view.findViewById(R.id.ln_new_group).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddNewRoomActivity.class));
            drawerLayout.closeDrawers();
        });

        nav_view.findViewById(R.id.settings_btn).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            drawerLayout.closeDrawers();
        });

        nav_view.findViewById(R.id.ll_policy).setOnClickListener(v -> {
            initPolicy();
            drawerLayout.closeDrawers();
        });

        nav_view.findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            drawerLayout.closeDrawers();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
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
        startActivity(intent);
    }
}