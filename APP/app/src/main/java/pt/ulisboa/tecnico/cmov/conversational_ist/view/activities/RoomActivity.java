package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    RequestQueue queue;
    List<Message> messageList;

    private final BroadcastReceiver Updated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                System.out.println("HEOEEKEFKEFKEEF\n");
                Message m = (Message) extras.get("message");
                messageList.add(m);
                adapterChat.notifyItemInserted(messageList.size() - 1);
            }
        }
    };


    private AdapterChat adapterChat;

    private String username = "bcv";
    private String roomID;

    private final String BASE_URL = "https://cmuapi.herokuapp.com/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // initialise the text views and layouts
        messageList = new ArrayList<>();
        name = findViewById(R.id.room_name);
        rId = findViewById(R.id.room_id);
        backBtn = findViewById(R.id.btn_back);

        msg = findViewById(R.id.ed_msg);
        send = findViewById(R.id.room_send_btn);
        attach = findViewById(R.id.btn_attach_file);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //********* DATA FROM MAIN ACTIVITY **********
        String roomName = getIntent().getStringExtra("roomName"); //TODO Nome da sala
        roomID = getIntent().getStringExtra("roomId");
        System.out.println("HERE: " + roomName + "\n");
        name.setText(roomName);
        rId.setText(roomID);
        //********************************************

        backBtn.setOnClickListener(v -> finish());

        queue = Volley.newRequestQueue(RoomActivity.this);

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

        registerReceiver(Updated, new IntentFilter("message_inserted_" + roomID));

    }

    private void loadMessages() {
        messageList = FeedReaderDbHelper.getInstance(getApplicationContext()).getAllMessages(roomID);
        if (messageList.isEmpty()) {
            fetchMessages();
            return;
        }
        initAdapter();
    }

    private void initAdapter() {
        adapterChat = new AdapterChat(RoomActivity.this, messageList, username);
        recyclerView.setAdapter(adapterChat);
    }

    private void sendMessage(final String message) {
        String url = BASE_URL + "/messages";

        Map<String, String> params = new HashMap<String, String>();
        params.put("sender", username);
        params.put("roomID", roomID);
        params.put("message", message);
        params.put("isPhoto", "false"); //TODO implement photo

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
        });

        queue.add(request);

    }

    private void fetchMessages() {

        String url = BASE_URL + "/messages?roomID=" + roomID;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        boolean isPhoto = Boolean.parseBoolean(jresponse.getString("isPhoto"));
                        Message msg = new Message(jresponse.getString("id"),
                                jresponse.getString("sender"),
                                jresponse.getString("roomID"),
                                jresponse.getString("message"),
                                jresponse.getString("createdAt"),
                                isPhoto);
                        FeedReaderDbHelper.getInstance(getApplicationContext()).createMessage(msg, false);
                        messageList.add(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                initAdapter();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
    }


    public void fetchPhoto(String message, String messageID) {
        String url = BASE_URL + "/photo?messageID=" + messageID;

        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                savePhotoFile(message, messageID, response);
            }
        },50,50, ImageView.ScaleType.CENTER, null, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void savePhotoFile(String message, String messageID, Bitmap bitmap){
        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path, message + messageID + ".jpg");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut);
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (IOException e) {
            Toast.makeText(RoomActivity.this, "Fail to save photo file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean isWifiEnabled() {
        WifiManager wifi_m = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifi_m.isWifiEnabled()) { // if user opened wifi
            WifiInfo wifi_i = wifi_m.getConnectionInfo();
            return wifi_i.getNetworkId() != -1; // Not connected to any wifi device
        } else {
            return false; // user turned off wifi
        }
    }

    private void fetchMessagesBefore() {
        ArrayList<Message> chatList = new ArrayList<>();

        String last = ""; //TODO get last and sort

        String url = BASE_URL + "/messages/before?roomID=" + roomID + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
                System.out.println(response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jresponse = response.getJSONObject(i);
                        boolean isPhoto = Boolean.parseBoolean(jresponse.getString("isPhoto"));
                        Message msg = new Message(jresponse.getString("id"),
                                jresponse.getString("sender"),
                                jresponse.getString("roomID"),
                                jresponse.getString("message"),
                                jresponse.getString("createdAt"),
                                isPhoto);
                        FeedReaderDbHelper.getInstance(getApplicationContext()).createMessage(msg, false);
                        chatList.add(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(RoomActivity.this, "Data " + chatList.get(0), Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onStop() {
        super.onStop();
        NotifyActive.getInstance().setActive("");
    }


    @Override
    protected void onResume() {
        super.onResume();
        NotifyActive.getInstance().setActive(roomID);
    }


}