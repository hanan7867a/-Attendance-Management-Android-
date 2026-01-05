package com.example.attandence_managment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.List;

public class TodayClassesAdapter extends RecyclerView.Adapter<TodayClassesAdapter.ViewHolder> {

    private List<TodayClassModel> classList;

    public TodayClassesAdapter(List<TodayClassModel> classList) {
        this.classList = classList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_class, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TodayClassModel model = classList.get(position);

        holder.courseName.setText(model.courseName + " (" + model.section + ")");
        holder.time.setText(model.startTime + " - " + model.endTime);
        holder.venue.setText("Venue: " + model.venue);
        holder.teacherName.setText(model.teacherName);
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView courseName, time, venue,teacherName;

        ViewHolder(View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseName);
            time = itemView.findViewById(R.id.time);
            venue = itemView.findViewById(R.id.venue);
            teacherName=itemView.findViewById(R.id.teacherName);
        }
    }
}
