package com.example.attandence_managment.Student_module;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceHistory";

    private RecyclerView attendanceRecycler;
    private AttendanceAdapter adapter;
    private ArrayList<AttendanceItem> attendanceList;

    private FirebaseFirestore db;

    private String courseId;
    private String courseName,name,email,profilePic;
    private String studentId;
    ImageView profileImage;


    // Summary TextViews
    private TextView tvTotal, tvPresent, tvAbsent,studentName,studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_sheet);

        // 🔹 Get data from intent getIntent().getStringExtra("courseId");
        // getIntent().getStringExtra("courseName");
        courseId =getIntent().getStringExtra("courseId");
        Log log = null;
        log.d(TAG, "Course ID: " + courseId);

        courseName = getIntent().getStringExtra("courseName");
        name=getIntent().getStringExtra("name");
        email=getIntent().getStringExtra("email");
        profilePic=getIntent().getStringExtra("image");
        profileImage=findViewById(R.id.profile_image);




        // 🔹 Logged-in student id


        db = FirebaseFirestore.getInstance();

        attendanceRecycler = findViewById(R.id.attendanceRecycler);
        attendanceRecycler.setLayoutManager(new LinearLayoutManager(this));

        attendanceList = new ArrayList<>();
        adapter = new AttendanceAdapter(attendanceList);
        attendanceRecycler.setAdapter(adapter);

        tvTotal = findViewById(R.id.tvTotal);
        tvPresent = findViewById(R.id.tvPresent);
        tvAbsent = findViewById(R.id.tvAbsent);
        studentName=findViewById(R.id.name_of_student);
        studentEmail=findViewById(R.id.std_email);

        studentName.setText(name);
        studentEmail.setText(email);
        Glide.with(this)
                .load(profilePic)
                .placeholder(R.drawable.profilepicture)
                .into(profileImage);

        fetchStudentIdAndLoadAttendance();
    }
    private void fetchStudentIdAndLoadAttendance() {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        Toast.makeText(this, "User record not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 🔥 THIS is the REAL studentId used in attendance
                    studentId = userDoc.getString("studentId");

                    if (studentId == null || studentId.isEmpty()) {
                        Toast.makeText(this, "Student ID missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Now load attendance correctly
                    loadAttendance();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch student ID", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching studentId", e);
                });
    }


    private void loadAttendance() {

        db.collection("courses")
                .document(courseId.trim())
                .collection("attendance")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    attendanceList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {

                        String date = doc.getId(); // 🔥 date document
                        String status = doc.getString(this.studentId);

                        if (status != null) {
                            attendanceList.add(
                                    new AttendanceItem(
                                            date,
                                            courseName,
                                            status
                                    )
                            );
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateSummary();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error", e);
                });
    }

    private void updateSummary() {
        int present = 0;
        int absent = 0;

        for (AttendanceItem item : attendanceList) {
            if (item.getStatus().equalsIgnoreCase("Present")) {
                present++;
            } else {
                absent++;
            }
        }

        tvTotal.setText("Total: " + attendanceList.size());
        tvPresent.setText("Present: " + present);
        tvAbsent.setText("Absent: " + absent);
    }
}
