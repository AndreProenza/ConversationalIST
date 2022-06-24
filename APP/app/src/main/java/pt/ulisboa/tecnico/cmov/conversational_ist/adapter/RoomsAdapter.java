package pt.ulisboa.tecnico.cmov.conversational_ist.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import static android.content.ContentValues.TAG;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.AdapterChat;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewAddRoomsInterface;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.AddNewRoomActivity;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.MainActivity;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.RoomsActivity;


public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomsViewHolder> {
    Context context;
    ArrayList<Room> rooms;
    private final RecyclerViewAddRoomsInterface rvAddRoomsInterface;
    private String userID;

    private FloatingActionButton addRoomBtn;

    public RoomsAdapter(Context context, ArrayList<Room> rooms, RecyclerViewAddRoomsInterface rvAddRoomsInterface, String userID) {
        this.context = context;
        this.rooms = rooms;
        this.rvAddRoomsInterface = rvAddRoomsInterface;
        this.userID = userID;
    }

    public void setFilteredRooms(ArrayList<Room> filteredRooms) {
        this.rooms = filteredRooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.room_item, parent, false);
        return new RoomsViewHolder(v, rvAddRoomsInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomsViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.roomName.setText(room.getRoomName());
        holder.roomId.setText(room.getRoomId());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class RoomsViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, roomId;
        FloatingActionButton addRoomBtn;

        public RoomsViewHolder(@NonNull View itemView, RecyclerViewAddRoomsInterface rvAddRoomsInterface) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomId = itemView.findViewById(R.id.room_id);

            addRoomBtn = itemView.findViewById(R.id.add_room_btn);
            addRoomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (rvAddRoomsInterface != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            rvAddRoomsInterface.onItemClick(position);
                            Room r = rooms.get(position);
                            AddNewRoomActivity.postSubscribe(context,r.getRoomId(),userID);
                            FeedReaderDbHelper.getInstance(v.getContext()).createChannel(r);
                            FirebaseMessaging.getInstance().subscribeToTopic(roomId.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Subscribe successful");
                                    context.startActivity(new Intent(context, MainActivity.class));
                                    ((Activity)context).finish();
                                }
                            });
                        }

                    }

                }
            });
        }
    }
}
