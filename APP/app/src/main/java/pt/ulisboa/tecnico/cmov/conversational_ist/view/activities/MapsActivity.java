package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.databinding.ActivityMapsBinding;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton send_map;
    private LatLng loc2send;
    private SearchView map_searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pt.ulisboa.tecnico.cmov.conversational_ist.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_embed);

        send_map = findViewById(R.id.send_map);
        send_map.setVisibility(View.GONE);

        send_map.setOnClickListener(view -> {
            Intent intent = new Intent(MapsActivity.this, RoomActivity.class);
            intent.putExtra("message2send","https://www.google.com/maps/@"+loc2send.latitude+","+loc2send.longitude);
            setResult(505,intent);
            finish();
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient locationProvider = LocationServices.getFusedLocationProviderClient(this);
            locationProvider.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f));
                }
            });
        }
        mMap.setOnMapClickListener(arg0 -> {
            send_map.setVisibility(View.VISIBLE);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(arg0).title("Marker"));
            loc2send = arg0;
        });

        map_searchView = findViewById(R.id.map_searchView);

        map_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = map_searchView.getQuery().toString();

                List<Address> addressList = null;

                Geocoder geocoder = new Geocoder(MapsActivity.this);
                try {

                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    send_map.setVisibility(View.VISIBLE);
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));

                    loc2send = latLng;

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        map_searchView.setOnClickListener(v -> map_searchView.setIconified(false));
    }
}