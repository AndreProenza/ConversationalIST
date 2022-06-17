package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        initUser();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!userId.isEmpty()) { // If user already login
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class)); // Go to MainScreen
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, StartUpActivity.class)); // Go to Startup then Register
                }

                finish();
            }
        }, 2000);
    }

    private void initUser() {
        SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
        userId = sh.getString("saved_username","");
        Log.d("UserId: ", userId);
    }
}