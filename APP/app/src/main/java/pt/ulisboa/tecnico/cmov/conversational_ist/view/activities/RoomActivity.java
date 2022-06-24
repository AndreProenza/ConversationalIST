package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import pt.ulisboa.tecnico.cmov.conversational_ist.AdapterChat;
import pt.ulisboa.tecnico.cmov.conversational_ist.BuildConfig;
import pt.ulisboa.tecnico.cmov.conversational_ist.PhotoMultipartRequest;
import pt.ulisboa.tecnico.cmov.conversational_ist.PhotoUtils;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.NotifyActive;

public class RoomActivity extends AppCompatActivity {

    ImageButton backBtn;
    ImageButton btn_location;
    ImageButton btn_share;
    RecyclerView recyclerView;
    TextView name;
    TextView rId;
    TextView scrollButton;
    EditText msg;
    ImageButton send, attach;
    RequestQueue queue;
    List<Message> messageList;


    private final String deepLink = "https://github.com/AndreProenza/ConversationalIST/room/";

    private final BroadcastReceiver Updated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Message m = (Message) extras.get("message");
                if(m.getRoomID().equals(roomID)) {
                    messageList.add(m);
                    adapterChat.notifyItemInserted(messageList.size() - 1);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int pos = layoutManager.findLastVisibleItemPosition();
                    if(pos >= messageList.size() - 2) {
                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                    } else {
                        scrollButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };


    private AdapterChat adapterChat;
    private RecyclerView.OnScrollListener scrollListener;

    private String username;
    private String roomID;

    private ActivityResultLauncher<String> pickPhoto;
    private ActivityResultLauncher<Uri> takePhoto;
    private Uri photoTakenUri;


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

        scrollButton = findViewById(R.id.scroll_button);

        scrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(messageList.size() - 1);
                scrollButton.setVisibility(View.INVISIBLE);
                FeedReaderDbHelper.getInstance(getApplicationContext()).clearUnreadMessages(roomID);
            }
        });

        msg = findViewById(R.id.ed_msg);
        send = findViewById(R.id.room_send_btn);
        attach = findViewById(R.id.btn_attach_file);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                             @Override
                                             public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                                 super.onScrollStateChanged(recyclerView, newState);
                                                 int pos = linearLayoutManager.findLastVisibleItemPosition();
                                                 if(pos >= messageList.size() - 1) {
                                                     scrollButton.setVisibility(View.INVISIBLE);
                                                     FeedReaderDbHelper.getInstance(getApplicationContext()).clearUnreadMessages(roomID);
                                                 }
                                             }
                                         });


                btn_location = findViewById(R.id.btn_sticker);

        /*scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = linearLayoutManager.getItemCount();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();
                if ((visibleItemCount + pastVisiblesItems) < totalItemCount && pastVisiblesItems == 0) {
                    recyclerView.clearOnScrollListeners();
                    System.out.println("pedidoooooooo");
                    fetchMessagesBefore();
                }
            }
        };*/

        //recyclerView.addOnScrollListener(scrollListener);

        btn_location = findViewById(R.id.btn_sticker);
        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = new Intent(RoomActivity.this, MapsActivity.class).putExtra("markedPosition", new LatLng(-50, 100));
                startActivityForResult(it, 504);

            }
        });

        btn_share = findViewById(R.id.share_room_button);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                //sendIntent.putExtra("id", roomID);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, deepLink+roomID);
               //sendIntent.setType("vnd.android-dir/mms-sms");

                Intent shareIntent = Intent.createChooser(sendIntent, "Share room");
                startActivity(shareIntent);

            }
        });


        //********* DATA FROM MAIN ACTIVITY **********
        String roomName = getIntent().getStringExtra("roomName");

        roomID = getIntent().getStringExtra("roomId");

        if(roomName == null || roomName.isEmpty()) {
            roomName = FeedReaderDbHelper.getInstance(this).getChannelName(roomID);
        }

        username = getIntent().getStringExtra("username");
        if(username == null){
            SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
            username = sh.getString("saved_username","");
        }

        System.out.println(username + " HERE: " + roomName + "\n");

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
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
        });


        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachDialog();
            }
        });

        pickPhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        sendPhotoMessage(result);
                    }
                });

        takePhoto = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        sendPhotoMessage(photoTakenUri);
                    }

                });

        loadMessages();

        registerReceiver(Updated, new IntentFilter("message_inserted"));

        FeedReaderDbHelper.getInstance(this).clearUnreadMessages(roomID);
    }

    public void sendPhotoMessage(Uri uri){

        String url = BASE_URL + "/messages";

        Map<String, String> params = new HashMap<String, String>();
        params.put("sender", username);
        params.put("roomID", roomID);
        params.put("message", "");
        params.put("isPhoto", "true");

        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    uploadImage(uri,response.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);

    }

    public void uploadImage(Uri uri, String messageID){
        File imageFile = new File(Objects.requireNonNull(PhotoUtils.createCopyAndReturnRealPath(getApplicationContext(), uri)));
        Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        PhotoUtils.savePhotoFile(getApplicationContext(),messageID,bmp);
        String url = BASE_URL + "/photos?messageID=" + messageID;
        PhotoMultipartRequest<Object> imageUploadReq = new PhotoMultipartRequest<Object>(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"ta dificl");
            }
        }, new Response.Listener<Object>() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG,"ta facil");
            }
        }, imageFile);
        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(imageUploadReq);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==504 && data!=null)
        {
            String message=data.getStringExtra("message2send");
            msg.setText(message);
        }
    }

    public void showAttachDialog() {
        CharSequence[] options = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(
                RoomActivity.this);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    String uuid = UUID.randomUUID().toString();
                    File outputDir = getCacheDir();
                    File file;
                    try
                    {
                        file = File.createTempFile( uuid, ".jpg", outputDir );
                        photoTakenUri = FileProvider.getUriForFile(
                                        RoomActivity.this,
                                getApplicationContext().getPackageName() + ".provider", file );
                    }
                    catch( IllegalArgumentException | IOException e )
                    {
                        Toast.makeText(RoomActivity.this, "Take picture not available!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    takePhoto.launch(photoTakenUri);
                } else if (options[item].equals("Choose from Library")) {
                    pickPhoto.launch("image/*");
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void loadMessages() {
        messageList = FeedReaderDbHelper.getInstance(getApplicationContext()).getAllMessages(roomID);
        if (messageList.isEmpty()) {
            fetchMessages();
            return;
        }
        initAdapter();
    }

    public void initAdapter() {
        adapterChat = new AdapterChat(RoomActivity.this, messageList, username);
        recyclerView.setAdapter(adapterChat);
    }

    private void sendMessage(final String message) {
        String url = BASE_URL + "/messages";

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
        });

        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);

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
        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);
    }

    private void fetchMessagesBefore() {
        ArrayList<Message> chatList = new ArrayList<>();

        String last = messageList.get(0).getCreatedAt();

        String url = BASE_URL + "/messages/before?roomID=" + roomID + "&last=" + last;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(RoomActivity.this, response.length() + " Messages received!", Toast.LENGTH_SHORT).show();
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

                messageList.addAll(0,chatList);
                adapterChat.notifyItemRangeInserted(0,chatList.size());
                adapterChat.notifyItemChanged(chatList.size());
                //recyclerView.addOnScrollListener(scrollListener);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RoomActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).getmRequestQueue().add(request);
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