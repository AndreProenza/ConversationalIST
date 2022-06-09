package pt.ulisboa.tecnico.cmov.conversational_ist;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView name;
    EditText msg;
    ImageButton send, attach;
    String uid;
    RequestQueue queue;

    private AdapterChat adapterChat;

    private String roomID = "628e1fa903146c7d0cc43b23";
    private String token = "";
    private String last = "2022-05-25T12:26:00Z";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        queue = Volley.newRequestQueue(ChatActivity.this);

        // initialise the text views and layouts
        name = findViewById(R.id.nameptv);
        msg = findViewById(R.id.messaget);
        send = findViewById(R.id.sendmsg);
        attach = findViewById(R.id.attachbtn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.chatrecycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        uid = getIntent().getStringExtra("uid");


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msg.getText().toString().trim();
                if (TextUtils.isEmpty(message)) { //if empty
                    Toast.makeText(ChatActivity.this, "Please Write Something Here", Toast.LENGTH_LONG).show();
                } else {
                    sendMessage(message);
                }
            }
        });

        readMessages();
    }

    private void readMessagesBefore() {
        ArrayList<ModelChat> chatList = new ArrayList<>();

        String url = "http://10.0.2.2:8080/api/messages/before?roomID=" + roomID + "&token=" + token + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(ChatActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChatActivity.this, "Data " + chatList.get(0), Toast.LENGTH_SHORT).show();
                adapterChat = new AdapterChat(ChatActivity.this, chatList);
                adapterChat.notifyDataSetChanged();
                recyclerView.setAdapter(adapterChat);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    private void readMessages() {
        ArrayList<ModelChat> chatList = new ArrayList<>();

        String url = "http://10.0.2.2:8080/api/messages?roomID=" + roomID + "&token=" + token + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(ChatActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChatActivity.this, "Data " + chatList.get(0), Toast.LENGTH_SHORT).show();
                adapterChat = new AdapterChat(ChatActivity.this, chatList);
                adapterChat.notifyDataSetChanged();
                recyclerView.setAdapter(adapterChat);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
    }

    private void sendMessage(final String message) {
        String url = "https://cmuapi.herokuapp.com/api/messages";

        //TODO arguments
        Map<String, String> params = new HashMap<String, String>();
        params.put("sender", "bcv");
        params.put("roomID", "628e16d233ad036d14ee279a");
        params.put("message", message);
        params.put("token", "");
        params.put("isPhoto", "false");

        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                msg.getText().clear();

                Toast.makeText(ChatActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) ;

        queue.add(request);

    }

}

