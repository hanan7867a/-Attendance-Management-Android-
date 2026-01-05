package com.example.attandence_managment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.Student_module.AttendanceHistoryActivity;
import com.example.attandence_managment.grades.GradeBookOptionsActivity;
import com.example.attandence_managment.grades.SubmissionPageActivity;
import com.example.attandence_managment.teacher_side.ActivityAddStudentMarks;
import com.example.attandence_managment.teacher_side.CreateAssignment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Activity to display all courses for student or teacher
 */
public class AllClasses extends AppCompatActivity {

    private static final String TAG = "AllClasses";

    private RecyclerView subjectRecycler;
    private ArrayList<CourseModel> subjects;
    private SubjectAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String mode, name, email, profilePic, teacherImgUrl;

    private TextView studentName, studentEmail;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allclasses);

        // Retrieve intent data
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        profilePic = getIntent().getStringExtra("image");
        teacherImgUrl = getIntent().getStringExtra("teacherimg");
        mode = getIntent().getStringExtra("mode");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile(currentUser.getUid());
    }

    private void initViews() {
        subjectRecycler = findViewById(R.id.subjectRecycler);
        studentName = findViewById(R.id.name_of_student);
        studentEmail = findViewById(R.id.std_email);
        profileImage = findViewById(R.id.profile_image);

        studentName.setText(name);
        studentEmail.setText(email);
    }

    private void setupRecyclerView() {
        subjects = new ArrayList<>();
        adapter = new SubjectAdapter(subjects);
        subjectRecycler.setLayoutManager(new LinearLayoutManager(this));
        subjectRecycler.setAdapter(adapter);
    }

    /**
     * Load profile and courses based on mode
     */
    private void loadUserProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ("createass".equals(mode) || "addmarks".equals(mode)) {
                        // Teacher mode
                        String teacherId = "teacher987"; // Replace with actual teacherId if needed
                        CourseModel.fetchTeacherCourses(db, teacherId, subjects, adapter, this);
                        loadImage(profileImage, teacherImgUrl);
                    } else {
                        // Student mode
                        String studentId = doc.getString("studentId");
                        loadImage(profileImage, profilePic);
                        if (studentId != null) {
                            loadStudentCourses(studentId);
                        } else {
                            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to get user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Load image using Glide safely
     */
    private void loadImage(ImageView imageView, String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.profilepicture)
                .error(R.drawable.profilepicture)
                .circleCrop()
                .into(imageView);
    }

    // ----------------- Student Courses -----------------
    private void loadStudentCourses(String studentId) {
        db.collection("courses")
                .whereArrayContains("students", studentId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    subjects.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        String courseId = doc.getId();
                        String courseName = doc.getString("courseName");
                        String section = doc.getString("section");
                        if (courseName != null) {
                            String display = courseName + (section != null && !section.isEmpty() ? " (" + section + ")" : "");
                            subjects.add(new CourseModel(courseId, display));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load courses: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ----------------- MODEL CLASS -----------------
    static class CourseModel {
        String courseId;
        String displayName;

        CourseModel(String courseId, String displayName) {
            this.courseId = courseId;
            this.displayName = displayName;
        }

        /**
         * Fetch courses for a teacher
         */
        public static void fetchTeacherCourses(FirebaseFirestore db,
                                               String teacherId,
                                               ArrayList<CourseModel> targetList,
                                               RecyclerView.Adapter adapter,
                                               AppCompatActivity context) {

            db.collection("courses")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        targetList.clear();
                        if (querySnapshots.isEmpty()) {
                            Toast.makeText(context, "You are not teaching any courses", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (DocumentSnapshot doc : querySnapshots) {
                            String courseId = doc.getId();
                            String courseName = doc.getString("courseName");
                            String section = doc.getString("section");
                            String display = courseName + (section != null && !section.isEmpty() ? " (" + section + ")" : "");
                            targetList.add(new CourseModel(courseId, display));
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to load courses: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // ----------------- ADAPTER -----------------
    class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

        private final ArrayList<CourseModel> subjectList;

        SubjectAdapter(ArrayList<CourseModel> list) {
            this.subjectList = list;
        }

        @NonNull
        @Override
        public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new SubjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
            CourseModel course = subjectList.get(position);
            holder.subjectName.setText(course.displayName);

            holder.itemView.setOnClickListener(v -> handleCourseClick(course));
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        class SubjectViewHolder extends RecyclerView.ViewHolder {
            TextView subjectName;

            SubjectViewHolder(@NonNull View itemView) {
                super(itemView);
                subjectName = itemView.findViewById(R.id.subjectName);
            }
        }
    }

    /**
     * Handle click on a course item based on mode
     */
    private void handleCourseClick(CourseModel course) {
        Intent intent;
        switch (mode) {
            case "grades":
                intent = new Intent(this, GradeBookOptionsActivity.class);
                intent.putExtra("subject_id", course.courseId);
                intent.putExtra("subject_name", course.displayName);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("image", profilePic);
                startActivity(intent);
                break;
            case "attendance":
                intent = new Intent(this, AttendanceHistoryActivity.class);
                intent.putExtra("courseId", course.courseId);
                intent.putExtra("courseName", course.displayName);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("image", profilePic);
                startActivity(intent);
                break;
            case "submission":
                intent = new Intent(this, SubmissionPageActivity.class);
                intent.putExtra("courseId", course.courseId);
                intent.putExtra("courseName", course.displayName);
                startActivity(intent);
                break;
            case "createass":
                intent = new Intent(this, CreateAssignment.class);
                intent.putExtra("courseId", course.courseId);
                intent.putExtra("courseName", course.displayName);
                intent.putExtra("image", teacherImgUrl);
                startActivity(intent);
                break;
            case "addmarks":
                intent = new Intent(this, ActivityAddStudentMarks.class);
                intent.putExtra("courseId", course.courseId);
                intent.putExtra("courseName", course.displayName);
                intent.putExtra("image", teacherImgUrl);
                Log.d(TAG, "senddata: courseName=" + course.displayName + ", courseId=" + course.courseId);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "Invalid mode", Toast.LENGTH_SHORT).show();
        }
    }
}
