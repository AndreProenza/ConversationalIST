package pt.ulisboa.tecnico.conversationalist.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import pt.ulisboa.tecnico.conversationalist.R;

public class RoomActivity extends AppCompatActivity {

    private ImageButton sendBtn;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        initClicks();
    }

    private void initClicks() {
        sendBtn.findViewById(R.id.room_send_btn);
        backBtn.findViewById(R.id.btn_back);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SEND MESSAGE
            }
        });

        backBtn.setOnClickListener(v -> finish());
    }
}