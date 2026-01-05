package com.example.attandence_managment.announcments;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private final List<AnnouncementModel> announcements;
    private final Context context;

    public AnnouncementAdapter(Context context, List<AnnouncementModel> announcements) {
        this.context = context;
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnnouncementModel ann = announcements.get(position);

        holder.title.setText(ann.getTitle());
        holder.description.setText(ann.getDescription());
        holder.date.setText(ann.getDate());

        if (ann.getImage() != null && !ann.getImage().isEmpty()) {
            Glide.with(context)
                    .load(ann.getImage())
                    .placeholder(R.drawable.barchar)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.barchar);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnnouncementDetailActivity.class);
            intent.putExtra("announcementId", ann.getId());
            context.startActivity(intent);
        });

        Log.d("ADAPTER", "Binding: " + ann.getTitle());
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.announcementImage);
            title = itemView.findViewById(R.id.announcementTitle);
            description = itemView.findViewById(R.id.announcementShortDesc);
            date = itemView.findViewById(R.id.announcementDate);
        }
    }
}
