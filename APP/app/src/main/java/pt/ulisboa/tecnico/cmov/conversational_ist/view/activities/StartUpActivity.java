package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;

public class StartUpActivity extends AppCompatActivity {

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        btn = (Button)findViewById(R.id.btn_start_up);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogin();
            }
        });
    }

    public void toLogin() {
        startActivity(new Intent(StartUpActivity.this, RegisterActivity.class));
        //finish();
    }
}