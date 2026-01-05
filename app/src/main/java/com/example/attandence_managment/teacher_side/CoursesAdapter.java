package com.example.attandence_managment.teacher_side;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// CoursesAdapter.java
public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {

    private List<String> courses;
    private OnCourseRemoveListener listener;

    public interface OnCourseRemoveListener {
        void onRemove(String course);
    }

    public CoursesAdapter(List<String> courses, OnCourseRemoveListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String course = courses.get(position);
        holder.textView.setText(course);
        holder.itemView.setOnLongClickListener(v -> {
            listener.onRemove(course);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}