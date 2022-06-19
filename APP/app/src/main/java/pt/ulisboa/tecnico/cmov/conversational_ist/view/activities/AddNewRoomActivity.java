package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class AddNewRoomActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText edRoomName, edRoomDescription;
    private Spinner spinner;
    //private DatabaseReference db;
    private TextView latitude, longitude, country, locality, address;
    private TextView latitudeText, longitudeText, countryText, localityText, addressText;
    private LinearLayout locationLayout;
    private ProgressBar progressBar;
    private FusedLocationProviderClient locationProvider;

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
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        country = findViewById(R.id.country);
        locality = findViewById(R.id.locality);
        address = findViewById(R.id.address);
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
                        latitudeText.setText(Double.toString(addresses.get(0).getLatitude()));
                        longitudeText.setText(Double.toString(addresses.get(0).getLongitude()));
                        countryText.setText(addresses.get(0).getCountryName());
                        localityText.setText(addresses.get(0).getLocality());
                        addressText.setText(addresses.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initClick() {
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
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
                    //ADD HERE ROOM
                    locationLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
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
}