package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;

public class RecommendationsActivity extends AppCompatActivity {

    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(v -> finish());
    }

}