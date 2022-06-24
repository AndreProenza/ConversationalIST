package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.RoomType;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.adapter.RoomsAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewAddRoomsInterface;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class RecommendationsActivity extends AppCompatActivity implements RecyclerViewAddRoomsInterface {

    private ImageButton backBtn;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(v -> finish());

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_rooms);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
        String userID = sh.getString("saved_userid","");

        fetchRecommendations(userID);
    }

    private void fetchRecommendations(String userID) {
        ArrayList<Room> rooms = new ArrayList<>();

        String url = "https://cmuapi.herokuapp.com/api/rooms/recommendations?userID=" + userID;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RecommendationsActivity.this, "Recommendations received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        boolean isGeoFenced = Integer.parseInt(jresponse.getString("roomType")) == RoomType.GEOFENCED.ordinal();
                        Room room = new Room(jresponse.getString("id"), jresponse.getString("name"), isGeoFenced, Double.parseDouble(jresponse.getString("lat")), Double.parseDouble(jresponse.getString("lng")), Integer.parseInt(jresponse.getString("radius")), 0);
                        if(FeedReaderDbHelper.getInstance(getApplicationContext()).isChannelSubscribed(jresponse.getString("id"))) {
                            System.out.println("Name: " + jresponse.getString("name") + "; ID: " + jresponse.getString("id") +"\n");
                            rooms.add(room);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                RoomsAdapter roomsAdapter = new RoomsAdapter(RecommendationsActivity.this, rooms, RecommendationsActivity.this,userID);
                recyclerView.setAdapter(roomsAdapter);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RecommendationsActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);
    }

    @Override
    public void onItemClick(int position) {
        finish();
        startActivity(new Intent(RecommendationsActivity.this, MainActivity.class));
    }
}