package pt.ulisboa.tecnico.cmov.conversational_ist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderContract;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;


public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView name;
    EditText msg;
    ImageButton send, attach;
    String uid;
    RequestQueue queue;
    List<ModelChat> messageList;

    private final BroadcastReceiver Updated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                messageList.add(new ModelChat(extras.getString("message"),extras.getString("sender"),extras.getString("date")));
                adapterChat.notifyItemInserted(messageList.size() - 1);
            }
        }
    };

    private AdapterChat adapterChat;

    private String username = "bcv";
    private String roomID = "628e1fa903146c7d0cc43b23";
    private String last = "2022-05-25T12:26:19.398+00:00";

    private final String BASE_URL = "https://cmuapi.herokuapp.com/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        queue = Volley.newRequestQueue(ChatActivity.this);

        // initialise the text views and layouts
        messageList = new ArrayList<>();
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

        loadMessages();

        registerReceiver(Updated, new IntentFilter("message_inserted_"+roomID));
    }

    private void loadMessages() {

        SQLiteDatabase db = FeedReaderDbHelper.getInstance(getApplicationContext()).getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME + " WHERE roomID = '" + roomID + "';",null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                messageList.add(new ModelChat(cursor.getString(3),cursor.getString(1),cursor.getString(4)));
                cursor.moveToNext();
            }
        }

        /*if(!messageList.isEmpty())
            last = messageList.get(messageList.size()-1).getTimestamp();*/ //TODO translate timestamp

        adapterChat = new AdapterChat(ChatActivity.this, messageList);
        adapterChat.notifyDataSetChanged();
        recyclerView.setAdapter(adapterChat);

        readMessages();

    }

   /* private void readMessagesBefore() {
        ArrayList<ModelChat> chatList = new ArrayList<>();

        String url = BASE_URL + "/messages/before?roomID=" + roomID + "&last=" + last;

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
    }*/



    private void readMessages() {

        String url = BASE_URL + "/messages?roomID=" + roomID + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(ChatActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        ModelChat modelChat = new ModelChat(jresponse.getString("message"), jresponse.getString("sender"), jresponse.getString("createdAt"));
                        Message msg = new Message(jresponse.getString("id"),
                                jresponse.getString("sender"),
                                jresponse.getString("roomID"),
                                jresponse.getString("message"),
                                jresponse.getString("createdAt"),
                                0);
                        FeedReaderDbHelper.getInstance(getApplicationContext()).createMessage(msg, false);
                        messageList.add(modelChat);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                adapterChat.notifyDataSetChanged();

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

