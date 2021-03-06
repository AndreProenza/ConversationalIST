package pt.ulisboa.tecnico.cmov.conversational_ist.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.interfaces.RecyclerViewEnterChatInterface;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class MainRoomsAdapter extends RecyclerView.Adapter<MainRoomsAdapter.RoomsViewHolder> {
    Context context;
    ArrayList<Room> rooms;
    private final RecyclerViewEnterChatInterface rvEnterChatInterface;

    public MainRoomsAdapter(Context context, ArrayList<Room> rooms, RecyclerViewEnterChatInterface rvEnterChatInterface) {
        this.context = context;
        this.rooms = rooms;
        this.rvEnterChatInterface = rvEnterChatInterface;
    }

    @NonNull
    @Override
    public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.room_item_main, parent, false);
        return new RoomsViewHolder(v, rvEnterChatInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RoomsViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.roomName.setText(room.getRoomName());
        holder.roomId.setText(room.getRoomId());
        if(room.getUnreadNum() > 0) {
            holder.notif_badge.setVisibility(View.VISIBLE);
            if(room.getUnreadNum() > 99) {
                holder.notif_badge.setText("99+");
            } else {
                holder.notif_badge.setText(String.valueOf(room.getUnreadNum()));
            }
        } else {
            holder.notif_badge.setVisibility(View.GONE);
        }
        //holder.roomDescription.setText(room.getRoomDescription());
        //holder.roomVisibility.setText(room.getRoomVisibility());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public ArrayList<Room> getList() {
        return rooms;
    }

    public static class RoomsViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, roomId, roomDescription, roomVisibility, notif_badge;
        public RoomsViewHolder(@NonNull View itemView,  RecyclerViewEnterChatInterface rvEnterChatInterface) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomId = itemView.findViewById(R.id.room_id);
            notif_badge = itemView.findViewById(R.id.notif_badge);

            itemView.setOnClickListener(v -> {
                if (rvEnterChatInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        rvEnterChatInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
