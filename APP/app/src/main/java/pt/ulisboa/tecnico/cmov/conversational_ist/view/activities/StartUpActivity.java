package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        Button btn = (Button) findViewById(R.id.btn_start_up);
        btn.setOnClickListener(v -> toRegister());
    }

    public void toRegister() {
        startActivity(new Intent(StartUpActivity.this, RegisterActivity.class));
        finish();
    }
}