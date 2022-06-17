package pt.ulisboa.tecnico.cmov.conversational_ist;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.RoomActivity;


public class AdapterChat extends RecyclerView.Adapter<pt.ulisboa.tecnico.cmov.conversational_ist.AdapterChat.Myholder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<Message> list;
    private ContentResolver resolver;

    private final String username;

    public AdapterChat(Context context, ContentResolver resolver, List<Message> list, String username) {
        this.context = context;
        this.list = list;
        this.username = username;
        this.resolver = resolver;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new Myholder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new Myholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, @SuppressLint("RecyclerView") final int position) {
        String messageID = list.get(position).getId();
        String message = list.get(position).getMessage();
        String timeStamp = list.get(position).getCreatedAt();
        boolean isPhoto = list.get(position).isPhoto();
        holder.message.setText(message);
        holder.time.setText(timeStamp);

        if (!isPhoto) {
            holder.message.setVisibility(View.VISIBLE);
            holder.mimage.setVisibility(View.GONE);
            holder.message.setText(message);
        } else {
            holder.message.setVisibility(View.GONE);
            holder.mimage.setVisibility(View.VISIBLE);
            try {
                Bitmap image = getPhotoFromMedia(messageID);
                holder.mimage.setImageBitmap(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //TODO set placeholder and change line below
                fetchPhoto(messageID,position);
                if(isWifiEnabled()){

                }
            }
        }
    }

    public Bitmap getPhotoFromMedia(String messageID) throws FileNotFoundException {
        Bitmap b = BitmapFactory.decodeStream(context.openFileInput(messageID + ".jpg"));
        return b;
    }

    private boolean isWifiEnabled() {
        WifiManager wifi_m = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifi_m.isWifiEnabled()) { // if user opened wifi
            WifiInfo wifi_i = wifi_m.getConnectionInfo();
            return wifi_i.getNetworkId() != -1; // Not connected to any wifi device
        } else {
            return false; // user turned off wifi
        }
    }

    public void fetchPhoto(String messageID, int position) {
        String url = "https://cmuapi.herokuapp.com/api/photos?messageID=" + messageID;

        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                savePhotoFile(messageID, response);
                notifyItemChanged(position);
            }
        },3000,3000, ImageView.ScaleType.CENTER, null, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(context).getmRequestQueue().add(imageRequest);
    }

    public void savePhotoFile(String messageID, Bitmap bitmap){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(messageID + ".jpg", Context.MODE_PRIVATE);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getSender().equals(username)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class Myholder extends RecyclerView.ViewHolder {

        ImageView mimage;
        TextView message, time;
        LinearLayout msglayput;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.msgc);
            time = itemView.findViewById(R.id.timetv);
            msglayput = itemView.findViewById(R.id.msglayout);
            mimage = itemView.findViewById(R.id.images);
        }
    }
}
