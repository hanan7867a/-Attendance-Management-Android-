package com.example.attandence_managment.teacher_side;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.AllClasses;
import com.example.attandence_managment.R;
import com.example.attandence_managment.announcments.AnnouncementAdapter;
import com.example.attandence_managment.announcments.AnnouncementModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeacherDashboardActivity extends AppCompatActivity {

    private static final String TAG = "TeacherDashboard";

    private TextView username, todaysClassesCount, createAssBtn;
    private Button marksBtn;
    private ImageView teacherImage;

    private RecyclerView recyclerClasses, recyclerAnnouncements;
    private TeacherClassAdapter classAdapter;
    private AnnouncementAdapter announcementAdapter;

    private List<TeacherClass> classList;
    private List<AnnouncementModel> announcementList;

    private FirebaseFirestore db;
    private String loggedInTeacherId = "teacher987"; // TODO: replace with FirebaseAuth UID
    private String profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_dashboard);

        initViews();
        initFirestore();
        setupClassRecycler();
        setupAnnouncementRecycler();
        setListeners();

        fetchTeacherProfile();
        fetchTodaysClasses();
    }

    private void initViews() {
        username = findViewById(R.id.username);
        todaysClassesCount = findViewById(R.id.todaysClassesCount);
        createAssBtn = findViewById(R.id.createassbtn);
        marksBtn = findViewById(R.id.marksbtn);
        teacherImage = findViewById(R.id.teacherimage);

        recyclerClasses = findViewById(R.id.myClassesRecycler);
        recyclerAnnouncements = findViewById(R.id.recyclerAnnouncements);
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupClassRecycler() {
        classList = new ArrayList<>();
        classAdapter = new TeacherClassAdapter(this, classList);
        recyclerClasses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerClasses.setAdapter(classAdapter);
    }

    private void setupAnnouncementRecycler() {
        announcementList = new ArrayList<>();
        announcementAdapter = new AnnouncementAdapter(this, announcementList);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnnouncements.setAdapter(announcementAdapter);
    }

    private void setListeners() {
        createAssBtn.setOnClickListener(v -> openAllClasses("createass"));
        marksBtn.setOnClickListener(v -> openAllClasses("addmarks"));
    }

    private void openAllClasses(String mode) {
        Intent intent = new Intent(this, AllClasses.class);
        intent.putExtra("mode", mode);
        intent.putExtra("teacherimg", profilePic);
        startActivity(intent);
    }

    /** Fetch teacher profile and display greeting & image */
    private void fetchTeacherProfile() {
        db.collection("users")
                .whereEqualTo("teacherId", loggedInTeacherId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        String name = doc.getString("name");
                        profilePic = doc.getString("imgurl");

                        if (profilePic != null && !profilePic.isEmpty()) {
                            loadImage(teacherImage, profilePic);
                        }

                        username.setText(name != null && !name.isEmpty() ? "Good morning, " + name : "Good morning!");
                        Log.d(TAG, "Teacher fetched: " + name);
                    } else {
                        username.setText("Good morning!");
                        Log.w(TAG, "No teacher found with ID: " + loggedInTeacherId);
                    }
                })
                .addOnFailureListener(e -> {
                    username.setText("Good morning!");
                    Log.e(TAG, "Failed to fetch teacher profile", e);
                    Toast.makeText(this, "Failed to fetch profile", Toast.LENGTH_SHORT).show();
                });
    }

    /** Fetch today's classes for teacher */
    private void fetchTodaysClasses() {
        String today = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());

        db.collection("courses")
                .whereEqualTo("teacherId", loggedInTeacherId)
                .get()
                .addOnSuccessListener(query -> {
                    classList.clear();

                    for (DocumentSnapshot courseDoc : query.getDocuments()) {
                        String courseId = courseDoc.getId();
                        String courseName = courseDoc.getString("courseName");
                        String section = courseDoc.getString("section");
                        List<String> students = (List<String>) courseDoc.get("students");

                        // Fetch schedule for today
                        db.collection("courses")
                                .document(courseId)
                                .collection("schedule")
                                .whereEqualTo("day", today)
                                .get()
                                .addOnSuccessListener(scheduleSnapshots -> {
                                    for (DocumentSnapshot scheduleDoc : scheduleSnapshots) {
                                        String venue = scheduleDoc.getString("venue");
                                        String startTime = scheduleDoc.getString("startTime");
                                        String endTime = scheduleDoc.getString("endTime");

                                        classList.add(new TeacherClass(
                                                courseId,
                                                courseName,
                                                section,
                                                venue,
                                                startTime,
                                                endTime,
                                                students != null ? students.size() : 0
                                        ));
                                    }
                                    classAdapter.notifyDataSetChanged();
                                    todaysClassesCount.setText(String.valueOf(classList.size()));
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch schedule", e));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch courses", e);
                    Toast.makeText(this, "Failed to fetch classes", Toast.LENGTH_SHORT).show();
                });
    }

    /** Load announcements */
    private void loadAnnouncements() {
        db.collection("announcements")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    announcementList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String desc = doc.getString("description");
                        String image = doc.getString("image");
                        String fileAttachment = doc.getString("fileAttachment");
                        String postedBy = doc.getString("postedBy");
                        String date = doc.getTimestamp("date") != null ?
                                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        .format(doc.getTimestamp("date").toDate()) : "";

                        announcementList.add(new AnnouncementModel(id, title, desc, image, fileAttachment, postedBy, date));
                    }
                    announcementAdapter.notifyDataSetChanged();
                    expandRecyclerViewFully(recyclerAnnouncements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load announcements", e);
                    Toast.makeText(this, "Failed to load announcements", Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnnouncements();
    }

    /** Glide helper */
    private void loadImage(ImageView imageView, String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.profilepicture)
                .error(R.drawable.profilepicture)
                .circleCrop()
                .into(imageView);
    }

    /** Expand RecyclerView to full height for nested scrolling */
    private void expandRecyclerViewFully(RecyclerView recyclerView) {
        recyclerView.post(() -> {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter == null) return;

            int totalHeight = 0;
            for (int i = 0; i < adapter.getItemCount(); i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                totalHeight += holder.itemView.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = totalHeight;
            recyclerView.setLayoutParams(params);
            recyclerView.requestLayout();
        });
    }
}
