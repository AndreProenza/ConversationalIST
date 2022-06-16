package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.conversational_ist.AdapterChat;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.NotifyActive;

public class RoomActivity extends AppCompatActivity {
    
    ImageButton backBtn;
    RecyclerView recyclerView;
    TextView name;
    TextView rId;
    EditText msg;
    ImageButton send, attach;
    String uid;
    RequestQueue queue;
    List<Message> messageList;

    /**
    private final BroadcastReceiver Updated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Message m = (Message) extras.get("message");
                messageList.add(m);
                adapterChat.notifyItemInserted(messageList.size() - 1);
            }
        }
    };
     */
    
    private AdapterChat adapterChat;

    private String username = "bcv";
    private String roomID = "628e1fa903146c7d0cc43b23";
    private String last = "2022-05-25T12:26:19.398+00:00";

    private final String BASE_URL = "https://cmuapi.herokuapp.com/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // initialise the text views and layouts
        messageList = new ArrayList<>();
        name = findViewById(R.id.room_name);
        rId = findViewById(R.id.room_id);
        //backBtn.findViewById(R.id.btn_back);
        /**
         msg = findViewById(R.id.ed_msg);
         send = findViewById(R.id.room_send_btn);
         attach = findViewById(R.id.btn_attach_file);
         LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
         linearLayoutManager.setStackFromEnd(true);
         recyclerView = findViewById(R.id.recycler_chat);
         recyclerView.setHasFixedSize(true);
         recyclerView.setLayoutManager(linearLayoutManager);
         uid = getIntent().getStringExtra("uid");
         */

        //********* DATA FROM MAIN ACTIVITY **********
        String roomName = getIntent().getStringExtra("roomName");
        String roomId = getIntent().getStringExtra("roomId");
        name.setText(roomName);
        rId.setText(roomId);
        //********************************************


        backBtn.setOnClickListener(v -> finish());

        /**
        Bundle b = getIntent().getExtras();
        if(b != null) {
            roomID = b.getString("roomID");
            System.out.println("Abriu notificao com roomID : " + roomID);
        }

        //TODO listen data changes

        queue = Volley.newRequestQueue(RoomActivity.this);

         */


        /**
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msg.getText().toString().trim();
                if (TextUtils.isEmpty(message)) { //if empty
                    Toast.makeText(RoomActivity.this, "Please Write Something Here", Toast.LENGTH_LONG).show();
                } else {
                    sendMessage(message);
                }
            }
        });

        loadMessages();

        registerReceiver(Updated, new IntentFilter("message_inserted_"+roomID));
         */
    }

    private void loadMessages() {
        messageList = FeedReaderDbHelper.getInstance(getApplicationContext()).getAllMessages(roomID);
        adapterChat = new AdapterChat(RoomActivity.this, messageList);
        recyclerView.setAdapter(adapterChat);
    }

    private void sendMessage(final String message) {
        String url = BASE_URL + "/messages";

        //TODO arguments
        Map<String, String> params = new HashMap<String, String>();
        params.put("sender", username);
        params.put("roomID", roomID);
        params.put("message", message);
        params.put("isPhoto", "false");

        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                msg.getText().clear();
                Toast.makeText(RoomActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) ;

        queue.add(request);

    }

    /*
    private void readMessagesBefore() {
        ArrayList<ModelChat> chatList = new ArrayList<>();

        String url = BASE_URL + "/messages/before?roomID=" + roomID + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        ModelChat modelChat = new ModelChat(jresponse.getString("message"), jresponse.getString("sender"), jresponse.getString("createdAt"));
                        chatList.add(modelChat);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(RoomActivity.this, "Data " + chatList.get(0), Toast.LENGTH_SHORT).show();
                adapterChat = new AdapterChat(RoomActivity.this, chatList);
                adapterChat.notifyDataSetChanged();
                recyclerView.setAdapter(adapterChat);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    private void readMessages() {

        String url = BASE_URL + "/messages?roomID=" + roomID + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        Message msg = new Message(jresponse.getString("id"),
                                jresponse.getString("sender"),
                                jresponse.getString("roomID"),
                                jresponse.getString("message"),
                                jresponse.getString("createdAt"),
                                0);
                        FeedReaderDbHelper.getInstance(getApplicationContext()).createMessage(msg, false);
                        messageList.add(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                adapterChat.notifyDataSetChanged();

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
    }
     */

    /**
    @Override
    protected void onStop() {
        super.onStop();
        NotifyActive.getInstance().setActive("");
    }
     */

    /**
    @Override
    protected void onResume() {
        super.onResume();
        NotifyActive.getInstance().setActive(roomID);
    }
    */
    
}