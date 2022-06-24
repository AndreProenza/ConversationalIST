package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class AddNewRoomActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText edRoomName;
    private Spinner spinner;
    //private DatabaseReference db;
    private double latitude, longitude;
    private String country, locality, address;
    private TextView latitudeText, longitudeText, countryText, localityText, addressText;
    private TextInputEditText radiusInputText;
    private String radius;
    private LinearLayout locationLayout;
    private ProgressBar progressBar;
    private FusedLocationProviderClient locationProvider;
    private int selectedRoomType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_room);

        init();
        initSpinnerAndLocation();
        initClick();
    }

    private void init() {
        edRoomName = findViewById(R.id.ed_room_name);
        //edRoomDescription = findViewById(R.id.ed_room_description);
        spinner = findViewById(R.id.sp_new_room);
    }

    private void initSpinnerAndLocation() {
        locationLayout = (LinearLayout) findViewById(R.id.location_ll);
        latitudeText = findViewById(R.id.latitude_coor);
        longitudeText = findViewById(R.id.longitude_coor);
        countryText = findViewById(R.id.country_coor);
        localityText = findViewById(R.id.locality_coor);
        addressText = findViewById(R.id.address_coor);

        locationProvider = LocationServices.getFusedLocationProviderClient(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.room_visibility, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoomType = position;
                switch (position) {
                    case 0:
                        locationLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        locationLayout.setVisibility(View.GONE);
                        break;
                    case 2:
                        locationLayout.setVisibility(View.VISIBLE);
                        if (ActivityCompat.checkSelfPermission(AddNewRoomActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            getLocation();
                        } else {
                            ActivityCompat.requestPermissions(AddNewRoomActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //spinner.setOnItemSelectedListener(this);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationProvider.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(AddNewRoomActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        //Set text to textviews
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();
                        country = addresses.get(0).getCountryName();
                        locality = addresses.get(0).getLocality();
                        address = addresses.get(0).getAddressLine(0);
                        latitudeText.setText(Double.toString(latitude));
                        longitudeText.setText(Double.toString(longitude));
                        countryText.setText(country);
                        localityText.setText(locality);
                        addressText.setText(address);

                        radiusInputText = findViewById(R.id.radius);
                        String radiusText = radiusInputText.getText().toString();

                        if (TextUtils.isEmpty(radiusText)){
                            radiusInputText.setError("Radius cannot be empty");
                            radiusInputText.requestFocus();
                        } else {
                            radius = radiusText;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initClick() {
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            // Go to Main activity
            startActivity(new Intent(AddNewRoomActivity.this, MainActivity.class));
            finish();
        });


        progressBar = findViewById(R.id.progress_bar);
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check Validate
                if (TextUtils.isEmpty(edRoomName.getText().toString())) {
                    edRoomName.setError("Field required!");
                } //else if (TextUtils.isEmpty(edRoomDescription.getText().toString())) {
                    //edRoomDescription.setError("Field required!");
                //}
                else if (spinner.getSelectedItem().toString() == null) {
                    Toast.makeText(getApplicationContext(), "Please Select Room visibility", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    //addRoomToFirebase();
                    locationLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    //TODO
                    //ADD HERE ROOM
                    //Use radius variable to store locality in heroku
                    //radius
                    if(selectedRoomType != 2) {
                        radius = "0";
                    } else {
                        radius = radiusInputText.getText().toString();
                    }
                    postRequestRoom(edRoomName.getText().toString(), selectedRoomType, latitude, longitude, radius );

                }
            }
        });
    }


    private void postRequestRoom(String name, int roomType, double lat, double lng, String radius) {
        String url = "https://cmuapi.herokuapp.com/api/rooms";

        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("roomType", String.valueOf(roomType));
        params.put("lat", Double.toString(lat));
        params.put("lng", Double.toString(lng));
        params.put("radius", radius);

        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String id = response.getString("id");
                    Room r = new Room(id,name,roomType==2,lat,lng,Integer.parseInt(radius));
                    FeedReaderDbHelper.getInstance(getApplicationContext()).createChannel(r);
                    FirebaseMessaging.getInstance().subscribeToTopic(id).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Subscribe successful");
                        }
                    });
                    // Go to Main activity
                    startActivity(new Intent(AddNewRoomActivity.this, MainActivity.class));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(AddNewRoomActivity.this, "Room added!", Toast.LENGTH_SHORT).show();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddNewRoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);

    }


    /**
    private void addRoomToFirebase() {
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
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Your Room is " + text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddNewRoomActivity.this, MainActivity.class));
        finish();
    }
}