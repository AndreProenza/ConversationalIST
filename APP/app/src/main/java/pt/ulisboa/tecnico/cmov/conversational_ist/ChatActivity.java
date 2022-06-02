package pt.ulisboa.tecnico.cmov.conversational_ist;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView name;
    EditText msg;
    ImageButton send, attach;
    String uid;
    RequestQueue queue;

    boolean notify = false;
    private AdapterChat adapterChat;

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
                notify = true;
                String message = msg.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {//if empty
                    Toast.makeText(ChatActivity.this, "Please Write Something Here", Toast.LENGTH_LONG).show();
                } else {
                    sendmessage(message);
                }
            }
        });

        readMessages();
    }

    private void readMessages() {
        // show message after retrieving data
        ArrayList<ModelChat> chatList = new ArrayList<>();

        String url = "https://cmuapi.herokuapp.com/api/messages";

        Map<String, String> params = new HashMap<String, String>();
        params.put("roomID", "628e16d233ad036d14ee279a");
        params.put("token", "");
        params.put("last", "2022-06-01T12:00:00Z");

        JSONObject jsonObj = new JSONObject(params);

        System.out.println("entrou");

        CustomJsonArrayRequest request = new CustomJsonArrayRequest(Request.Method.GET, url, jsonObj, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(ChatActivity.this, "Messages received!", Toast.LENGTH_SHORT).show();
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
                System.out.println("chega");
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", "ola");
                return params;
            }
        };

        queue.add(request);
    }

    private void sendmessage(final String message) {
        String url = "https://cmuapi.herokuapp.com/api/messages";

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
        }) {
            @Override
            protected Map<String, String> getParams() {
                // below line we are creating a map for
                // storing our values in key and value pair.
                Map<String, String> params = new HashMap<String, String>();

                // on below line we are passing our key
                // and value pair to our parameters.
                params.put("name", "ola");

                // at last we are
                // returning our params.
                return params;
            }
        };
        ;

        queue.add(request);

    }

}

