package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //***********************
        //FIREBASE AUTH
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseUser != null) { // If user already login
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class)); // Go to MainScreen
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, StartUpActivity.class)); // Go to Startup then Login
                }

                finish();
            }
        }, 2000);

        //**********************
    }
}