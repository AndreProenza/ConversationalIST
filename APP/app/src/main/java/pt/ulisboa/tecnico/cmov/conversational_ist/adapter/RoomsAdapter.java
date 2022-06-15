package pt.ulisboa.tecnico.conversationalist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.conversationalist.R;
import pt.ulisboa.tecnico.conversationalist.model.Room;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomsViewHolder> {
    Context context;
    ArrayList<Room> rooms;

    public RoomsAdapter(Context context, ArrayList<Room> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.room_item, parent, false);
        return new RoomsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomsViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.roomName.setText(room.getRoomName());
        holder.roomDescription.setText(room.getRoomDescription());
        holder.roomVisibility.setText(room.getRoomVisibility());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class RoomsViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, roomDescription, roomVisibility;
        public RoomsViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomDescription = itemView.findViewById(R.id.room_description);
            roomVisibility = itemView.findViewById(R.id.room_visibility);
        }
    }
}
