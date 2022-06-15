package pt.ulisboa.tecnico.cmov.conversational_ist.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.conversationalist.model.User;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.RoomActivity;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.Holder> {

    List<User> userModelList = new ArrayList<>();
    private Context context;

    public ContactsAdapter(List<User> userModelList) { this.userModelList = userModelList; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvName.setText(userModelList.get(position).getUsername());
        holder.tvStatus.setText("....");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               context.startActivity(new Intent(context, RoomActivity.class));
           }
        });
    }

    @Override
    public int getItemCount() { return userModelList.size(); }

    public static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName, tvStatus;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
