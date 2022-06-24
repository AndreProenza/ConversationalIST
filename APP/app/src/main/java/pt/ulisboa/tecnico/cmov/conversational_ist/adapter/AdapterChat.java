package pt.ulisboa.tecnico.cmov.conversational_ist.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import pt.ulisboa.tecnico.cmov.conversational_ist.PhotoUtils;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.VolleySingleton;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Myholder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private final Context context;
    private final List<Message> list;

    private final String username;

    public AdapterChat(Context context, List<Message> list, String username) {
        this.context = context;
        this.list = list;
        this.username = username;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_me, parent, false);
        }
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, @SuppressLint("RecyclerView") final int position) {
        AtomicReference<String> messageID = new AtomicReference<>(list.get(position).getId());
        String message = list.get(position).getMessage();
        String timeStamp = formatDate(list.get(position).getCreatedAt());
        boolean isPhoto = list.get(position).isPhoto();
        String sender = list.get(position).getSender();
        holder.message.setText(message);
        holder.time.setText(timeStamp);
        holder.username.setText(sender);

        if (!isPhoto) {
            if(message.startsWith("https://www.google.com/maps/@")) {

                holder.message.setVisibility(View.GONE);
                holder.mimage.setVisibility(View.GONE);
                holder.map.setVisibility(View.VISIBLE);
                holder.downloadBtn.setVisibility(View.GONE);

            } else {
                holder.message.setVisibility(View.VISIBLE);
                holder.mimage.setVisibility(View.GONE);
                holder.map.setVisibility(View.GONE);
                holder.downloadBtn.setVisibility(View.GONE);
                holder.message.setText(message);
            }
        } else {
            holder.message.setVisibility(View.GONE);
            holder.mimage.setVisibility(View.VISIBLE);
            holder.map.setVisibility(View.GONE);
            holder.downloadBtn.setVisibility(View.GONE);
            try {
                Bitmap image = getPhotoFromMedia(messageID.get());
                holder.mimage.setImageBitmap(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                if(isWifiEnabled()){
                    fetchPhoto(messageID.get(),position);
                }
                else {
                    holder.mimage.setImageResource(R.drawable.place_holder);
                    holder.downloadBtn.setVisibility(View.VISIBLE);
                    holder.downloadBtn.setOnClickListener(v -> fetchPhoto(messageID.get(),position));
                }
            }
        }
    }

    private String formatDate(String unformatted) {

        if(!unformatted.isEmpty()) {
            String[] result = unformatted.split("T");
            unformatted = result[0] + " at " + result[1].split(":")[0] + ":" + result[1].split(":")[1];
        }
        return unformatted;

    }

    public Bitmap getPhotoFromMedia(String messageID) throws FileNotFoundException {
        return BitmapFactory.decodeStream(context.openFileInput(messageID + ".jpg"));
    }

    @SuppressLint("MissingPermission")
    private boolean isWifiEnabled() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isWifiConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn = networkInfo.isConnected();
            }
        }
        return isWifiConn;
    }

    public void fetchPhoto(String messageID, int position) {
        String url = "https://cmuapi.herokuapp.com/api/photos?messageID=" + messageID;

        ImageRequest imageRequest = new ImageRequest(url, response -> {
            PhotoUtils.savePhotoFile(context, messageID, response);
            notifyItemChanged(position);
        },3000,3000, ImageView.ScaleType.CENTER, null, error -> Toast.makeText(context.getApplicationContext(), "Fail to get response = " + error, Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(context).getmRequestQueue().add(imageRequest);
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

    class Myholder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        ImageView mimage;
        GoogleMap gMap;
        MapView map;
        TextView username, message, time;
        ImageButton downloadBtn;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.message_username);
            message = itemView.findViewById(R.id.message_data);
            time = itemView.findViewById(R.id.message_date);
            mimage = itemView.findViewById(R.id.images);
            downloadBtn = itemView.findViewById(R.id.downloadImage);

            map = (MapView) itemView.findViewById(R.id.mapImageView);

            if (map != null)
            {
                map.onCreate(null);
                map.onResume();
                map.getMapAsync(this);
            }
        }




        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            MapsInitializer.initialize(context.getApplicationContext());
            gMap = googleMap;

            //you can move map here to item specific 'location'
            int pos = getAbsoluteAdapterPosition();
            //get 'location' by 'pos' from data list
            //then move to 'location'
            if(pos<0) {
                return;
            }
            String m = list.get(pos).getMessage();

            String[] half = m.split("@");

            if(half.length>1) {
                half = half[1].split(",");
                if(half.length>1) {
                    LatLng loc = new LatLng(Double.parseDouble(half[0]), Double.parseDouble(half[1]));

                    gMap.addMarker(new MarkerOptions().position(loc).title("Marker"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    //gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(half[0]), Double.parseDouble(half[1])), 16.0f));
                } else {
                    map.setVisibility(View.GONE);
                }
            } else {
            map.setVisibility(View.GONE);
            }
        }
    }
}
