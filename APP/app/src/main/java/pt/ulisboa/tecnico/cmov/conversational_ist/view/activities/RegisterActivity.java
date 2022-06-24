package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.firebase.FirebaseHandler;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    SharedPreferences sharedPrefMode;

    private TextInputEditText etRegUsername;
    private ProgressBar loadingPB;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyLightDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        etRegUsername = findViewById(R.id.etRegUsername);
        Button btnRegister = findViewById(R.id.btnRegister);
        loadingPB = findViewById(R.id.idLoadingPB);

        btnRegister.setOnClickListener(view -> createUser());
    }

    private void createUser(){
        String userName = Objects.requireNonNull(etRegUsername.getText()).toString();

        if (TextUtils.isEmpty(userName)){
            etRegUsername.setError("Username cannot be empty");
            etRegUsername.requestFocus();
        } else {
            // calling a method to post the data and passing our name and job.
            postDataUsingVolley(userName);
        }
    }


    private void postDataUsingVolley(String name) {
        // url to post our data
        String url = "https://cmuapi.herokuapp.com/api/users";
        loadingPB.setVisibility(View.VISIBLE);

        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);

        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj, response -> {
            loadingPB.setVisibility(View.GONE);

            Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
            try {
                String id = response.getString("id");
                String name1 = response.getString("name");
                saveUsername(name1,id);
                FirebaseHandler.registerUser(db, name1, name1);
                switchToMain();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            loadingPB.setVisibility(View.GONE);
            String body;
            //get status code here
            String statusCode = String.valueOf(error.networkResponse.statusCode);
            //get response body and parse with appropriate encoding
            if(error.networkResponse.data!=null) {
                body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Toast.makeText(RegisterActivity.this, "Registration Error: " + statusCode + " " + body, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(RegisterActivity.this, "Username already exists!", Toast.LENGTH_SHORT).show();
        });
        // below line is to make
        // a json object request.
        queue.add(request);
    }

    public void saveUsername(String username,String userID) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("saved_username", username);
        editor.putString("saved_userid", userID);
        editor.apply();
    }

    private void switchToMain() {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void verifyLightDarkMode() {
        sharedPrefMode = getSharedPreferences("mode", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPrefMode.getBoolean("mode_status", false);
        System.out.println("isDarkMode: " + isDarkMode);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}

