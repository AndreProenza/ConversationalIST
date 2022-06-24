package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.firebase.FirebaseHandler;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.User;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    SharedPreferences sharedPrefMode;

    private TextInputEditText etRegUsername;
    private Button btnRegister;
    private ProgressBar loadingPB;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyLightDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        etRegUsername = findViewById(R.id.etRegUsername);
        btnRegister = findViewById(R.id.btnRegister);
        loadingPB = findViewById(R.id.idLoadingPB);

        btnRegister.setOnClickListener(view ->{
            createUser();
        });
    }

    private void createUser(){
        String userName = etRegUsername.getText().toString();

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

        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);

        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj,new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);

                Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                try {
                    String id = response.getString("id");
                    String name = response.getString("name");
                    saveUsername(name,id);
                    FirebaseHandler.registerUser(db, name, name);
                    switchToMain();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingPB.setVisibility(View.GONE);
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        Toast.makeText(RegisterActivity.this, "Registration Error: " + statusCode + " " + body, Toast.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(RegisterActivity.this, "Username already exists!", Toast.LENGTH_SHORT).show();
            }
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

