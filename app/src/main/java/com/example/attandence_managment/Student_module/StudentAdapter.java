package com.example.attandence_managment.Student_module;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attandence_managment.R;
import com.example.attandence_managment.teacher_side.StudentItem;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final ArrayList<StudentItem> studentList;

    public StudentAdapter(ArrayList<StudentItem> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentItem student = studentList.get(position);

        holder.presentCheckbox.setOnCheckedChangeListener(null); // Remove old listener
        holder.name.setText(student.getName());
        holder.presentCheckbox.setChecked(student.isPresent());
        holder.presentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                student.setPresent(isChecked));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Switch presentCheckbox;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvStudentName);
            presentCheckbox = itemView.findViewById(R.id.switchAttendance);
        }
    }
}
