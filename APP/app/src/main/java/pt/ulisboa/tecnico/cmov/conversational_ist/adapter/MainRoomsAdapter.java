package pt.ulisboa.tecnico.cmov.conversational_ist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class MainRoomsAdapter extends RecyclerView.Adapter<MainRoomsAdapter.RoomsViewHolder> {
    Context context;
    ArrayList<Room> rooms;

    public MainRoomsAdapter(Context context, ArrayList<Room> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.room_item_main, parent, false);
        return new RoomsViewHolder(v);
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
        public RoomsViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomId = itemView.findViewById(R.id.room_id);
            //roomDescription = itemView.findViewById(R.id.room_description);
            //roomVisibility = itemView.findViewById(R.id.room_visibility);
        }
    }
}
