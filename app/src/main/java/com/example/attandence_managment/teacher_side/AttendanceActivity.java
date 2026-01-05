package com.example.attandence_managment.teacher_side;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";

    private TextView courseTitle;
    private RecyclerView studentsRecycler;
    private Button saveAttendanceBtn;

    private FirebaseFirestore db;
    private String courseId = "CS101-A"; // For now fixed course
    private String courseName = "Programming Fundamentals"; // For display

    private ArrayList<StudentItem> studentsList;
    private StudentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_activity);

        courseTitle = findViewById(R.id.tvCourseName);
        studentsRecycler = findViewById(R.id.rvStudentList);
        saveAttendanceBtn = findViewById(R.id.btnSaveAttendance);

        db = FirebaseFirestore.getInstance();

        courseTitle.setText(courseName);

        studentsList = new ArrayList<>();
        adapter = new StudentsAdapter(studentsList);
        studentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        studentsRecycler.setAdapter(adapter);

        loadStudents();

        saveAttendanceBtn.setOnClickListener(v -> saveAttendance());
    }

    private void loadStudents() {
        db.collection("courses")
                .document(courseId)
                .get()
                .addOnSuccessListener(courseDoc -> {

                    ArrayList<String> studentIds =
                            (ArrayList<String>) courseDoc.get("students");

                    if (studentIds == null || studentIds.isEmpty()) {
                        Toast.makeText(this, "No students enrolled", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    studentsList.clear();

                    for (String studentId : studentIds) {

                        db.collection("users")
                                .whereEqualTo("studentId", studentId)
                                .get()
                                .addOnSuccessListener(query -> {
                                    for (DocumentSnapshot userDoc : query) {

                                        String name = userDoc.getString("name");
                                        String regNo = userDoc.getString("studentId");
                                        String profilePic = userDoc.getString("profilePic");

                                        studentsList.add(
                                                new StudentItem(regNo, name, false, profilePic)
                                        );
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }



    private void saveAttendance() {
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        Map<String, String> attendanceMap = new HashMap<>();
        for (StudentItem student : studentsList) {
            attendanceMap.put(student.getStudentId(), student.isPresent() ? "Present" : "Absent");
        }

        db.collection("courses")
                .document(courseId)
                .collection("attendance")
                .document(today)
                .set(attendanceMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Attendance saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving attendance", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error", e);
                });
    }

    // ================= MODEL =================


    // ================= ADAPTER =================
    private class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.ViewHolder> {

        private final ArrayList<StudentItem> list;

        public StudentsAdapter(ArrayList<StudentItem> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_student_attendance, parent, false);
            return new ViewHolder(view);
        }

        @Override

        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudentItem student = list.get(position);

            holder.name.setText(student.getName() != null ? student.getName() : "N/A");
            holder.regNo.setText(student.getStudentId() != null ? student.getStudentId() : "N/A");
            Glide.with(holder.itemView.getContext())
                    .load(student.getImgurl())
                    .placeholder(R.drawable.profilepicture)
                    .error(R.drawable.profilepicture)
                    .circleCrop()
                    .into(holder.profilePic);


            holder.presentSwitch.setOnCheckedChangeListener(null);
            holder.presentSwitch.setChecked(student.isPresent());
            holder.presentSwitch.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> student.setPresent(isChecked)
            );
        }


        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView name,regNo;
            Switch presentSwitch;
            ImageView profilePic;


            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvStudentName);
                regNo = itemView.findViewById(R.id.tvStudentId);
                presentSwitch = itemView.findViewById(R.id.switchAttendance);
                profilePic=itemView.findViewById(R.id.imgProfile);
            }
        }
    }
}
