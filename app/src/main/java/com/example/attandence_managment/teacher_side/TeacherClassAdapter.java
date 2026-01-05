package com.example.attandence_managment.teacher_side;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attandence_managment.R;

import java.util.List;

public class TeacherClassAdapter extends RecyclerView.Adapter<TeacherClassAdapter.ViewHolder> {

    private Context context;
    private List<TeacherClass> classList;

    public TeacherClassAdapter(Context context, List<TeacherClass> classList) {
        this.context = context;
        this.classList = classList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_todayclasses, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherClass teacherClass = classList.get(position);

        holder.courseName.setText(teacherClass.getCourseName());
        holder.sectionVenue.setText("Section " + teacherClass.getSection() + " • " + teacherClass.getVenue());
        holder.studentCount.setText(teacherClass.getStudentCount() + " Students");
        holder.time.setText(teacherClass.getStartTime() + " - " + teacherClass.getEndTime());

        // First letter for icon
        holder.classIcon.setText(teacherClass.getCourseName().substring(0, 1).toUpperCase());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AttendanceActivity.class);
            intent.putExtra("courseId", teacherClass.getCourseId());
            intent.putExtra("courseName", teacherClass.getCourseName());
            context.startActivity(intent);
        });

        holder.btnTakeAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(context, AttendanceActivity.class);
            intent.putExtra("courseId", teacherClass.getCourseId());
            intent.putExtra("courseName", teacherClass.getCourseName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView classIcon, courseName, sectionVenue, studentCount, time;
        Button btnTakeAttendance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classIcon = itemView.findViewById(R.id.classIcon);
            courseName = itemView.findViewById(R.id.courseName);
            sectionVenue = itemView.findViewById(R.id.sectionVenue);
            studentCount = itemView.findViewById(R.id.studentCount);
            time = itemView.findViewById(R.id.time);
            btnTakeAttendance = itemView.findViewById(R.id.btnTakeAttendance);
        }
    }
}
