package com.example.attandence_managment.Student_module;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attandence_managment.R;

import java.util.ArrayList;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {



    private final ArrayList<AttendanceItem> list;

    public AttendanceAdapter(ArrayList<AttendanceItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AttendanceItem item = list.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvSubject.setText(item.getSubject());
        holder.tvStatus.setText(item.getStatus());

        // 🔹 Status color
        if (item.getStatus().equalsIgnoreCase("Present")) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_present);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_absent);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvSubject, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
