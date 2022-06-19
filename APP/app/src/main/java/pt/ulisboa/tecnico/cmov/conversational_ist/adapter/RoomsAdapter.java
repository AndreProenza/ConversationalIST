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

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewAddRoomsInterface;


public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomsViewHolder> {
    Context context;
    ArrayList<Room> rooms;
    private final RecyclerViewAddRoomsInterface rvAddRoomsInterface;

    private FloatingActionButton addRoomBtn;

    public RoomsAdapter(Context context, ArrayList<Room> rooms, RecyclerViewAddRoomsInterface rvAddRoomsInterface) {
        this.context = context;
        this.rooms = rooms;
        this.rvAddRoomsInterface = rvAddRoomsInterface;
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
        //holder.roomDescription.setText(room.getRoomDescription());
        //holder.roomVisibility.setText(room.getRoomVisibility());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class RoomsViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, roomId, roomDescription, roomVisibility;
        FloatingActionButton addRoomBtn;
        public RoomsViewHolder(@NonNull View itemView, RecyclerViewAddRoomsInterface rvAddRoomsInterface) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomId = itemView.findViewById(R.id.room_id);
            //roomDescription = itemView.findViewById(R.id.room_description);
            //roomVisibility = itemView.findViewById(R.id.room_visibility);

            addRoomBtn = itemView.findViewById(R.id.add_room_btn);
            addRoomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (rvAddRoomsInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            rvAddRoomsInterface.onItemClick(position);
                            FeedReaderDbHelper.getInstance(v.getContext()).createChannel(roomId.getText().toString(),roomName.getText().toString());
                            FirebaseMessaging.getInstance().subscribeToTopic(roomId.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Subscribe successful");
                                }
                            });
                        }

                    }

                }
            });
        }
    }
}
