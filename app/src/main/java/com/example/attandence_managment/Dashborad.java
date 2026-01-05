package com.example.attandence_managment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.announcments.AnnouncementAdapter;
import com.example.attandence_managment.announcments.AnnouncementModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Dashborad extends AppCompatActivity {

    private LinearLayout gradeRow, attandenceRow,submittionRow;
    private RelativeLayout todayClassesRow;
    private RecyclerView todayClassesRecycler, recyclerAnnouncements;
    private List<TodayClassModel> todayClassList;
    private TodayClassesAdapter todayAdapter;
    private List<AnnouncementModel> announcementList;
    private AnnouncementAdapter announcementAdapter;

    private ImageView profileImage;
    private TextView profileName, profileEmail,gretting_txt;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String studentId,announcId,profilePic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashborad);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Login check
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        setupTodayClassesRecycler();
        setupAnnouncementsRecycler();

        loadUserProfile(currentUser.getUid());

        //"
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.name_of_student);
        profileEmail = findViewById(R.id.std_email);

        submittionRow=findViewById(R.id.submittionRow);
        gradeRow = findViewById(R.id.gradeRow);
        attandenceRow = findViewById(R.id.attandenceRow);

        todayClassesRow = findViewById(R.id.todayClassesRow);
        todayClassesRecycler = findViewById(R.id.todayClassesRecycler);

        recyclerAnnouncements = findViewById(R.id.recyclerAnnouncements);
        gretting_txt=findViewById(R.id.gretting_txt);
    }

    private void setupClickListeners() {
        gradeRow.setOnClickListener(v -> {
            Intent intent = new Intent(Dashborad.this, AllClasses.class);
            intent.putExtra("mode", "grades");
            intent.putExtra("name", profileName.getText().toString());
            intent.putExtra("email", profileEmail.getText().toString());
            intent.putExtra("image", profilePic);
            startActivity(intent);
        });

        attandenceRow.setOnClickListener(v -> {
            Intent intent = new Intent(Dashborad.this, AllClasses.class);
            intent.putExtra("mode", "attendance");
            intent.putExtra("name", profileName.getText().toString());
            intent.putExtra("email", profileEmail.getText().toString());
            intent.putExtra("image", profilePic);
            startActivity(intent);
        });
        submittionRow.setOnClickListener(v->{
            Intent intent = new Intent(Dashborad.this, AllClasses.class);
            intent.putExtra("mode", "submission");
            intent.putExtra("name", profileName.getText().toString());
            intent.putExtra("email", profileEmail.getText().toString());
            intent.putExtra("image", profilePic);
            startActivity(intent);
        });


    }

    // ---------------- USER PROFILE ----------------
    private void loadUserProfile(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    profileName.setText(doc.getString("name"));
                    profileEmail.setText(doc.getString("email"));

                    profilePic = doc.getString("profilePic");
                    Log.d("profilePic", "profilePic: " + profilePic);

                    studentId = doc.getString("studentId");

                    Glide.with(this)
                            .load(profilePic)
                            .placeholder(R.drawable.profilepicture)
                            .circleCrop()
                            .into(profileImage);

                    if (studentId != null) {
                        loadTodayClasses(studentId);
                        loadAnnouncements(); // Load announcements after studentId is available
                    } else {
                        Toast.makeText(this, "studentId missing in user profile", Toast.LENGTH_LONG).show();
                    }
                    String text="Hi "+profileName.getText().toString()+"!";
                    gretting_txt.setText(text);
                });
    }

    // ---------------- TODAY CLASSES ----------------
    private void setupTodayClassesRecycler() {
        todayClassList = new ArrayList<>();
        todayAdapter = new TodayClassesAdapter(todayClassList);
        todayClassesRecycler.setLayoutManager(new LinearLayoutManager(this));
        todayClassesRecycler.setAdapter(todayAdapter);
    }

    private void loadTodayClasses(String studentId) {
        String today = new SimpleDateFormat("EEEE", Locale.getDefault())
                .format(new Date()).toLowerCase();

        db.collection("courses")
                .whereArrayContains("students", studentId)
                .get()
                .addOnSuccessListener(courseSnapshots -> {
                    todayClassList.clear();
                    for (DocumentSnapshot courseDoc : courseSnapshots) {
                        String courseName = courseDoc.getString("courseName");
                        String section = courseDoc.getString("section");
                        String teacherName = courseDoc.getString("teacherId");

                        courseDoc.getReference()
                                .collection("schedule")
                                .document(today)
                                .get()
                                .addOnSuccessListener(scheduleDoc -> {
                                    if (scheduleDoc.exists()) {
                                        todayClassList.add(new TodayClassModel(
                                                courseName,
                                                section,
                                                scheduleDoc.getString("startTime"),
                                                scheduleDoc.getString("endTime"),
                                                scheduleDoc.getString("venue"),
                                                teacherName
                                        ));
                                        todayAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    // ---------------- ANNOUNCEMENTS ----------------
    private void setupAnnouncementsRecycler() {
        announcementList = new ArrayList<>();
        announcementAdapter = new AnnouncementAdapter(this, announcementList);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnnouncements.setAdapter(announcementAdapter);
    }

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

    public void loadAnnouncements() {
        db.collection("announcements")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    Log.d("ANNOUNCEMENTS", "Total docs: " + querySnapshot.size());

                    announcementList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Log.d("ANNOUNCEMENTS", "Doc ID: " + doc.getId());
                        Log.d("ANNOUNCEMENTS", "Data: " + doc.getData());

                        String announcId = doc.getId();
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String image = doc.getString("image");
                        String fileAttachment = doc.getString("fileAttachment");
                        String postedBy = doc.getString("postedBy");

                        String date = "";
                        if (doc.getTimestamp("date") != null) {
                            date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(doc.getTimestamp("date").toDate());
                        }

                        announcementList.add(new AnnouncementModel(
                                announcId, title, description, image, fileAttachment, postedBy, date
                        ));
                    }

                    announcementAdapter.notifyDataSetChanged();
                    expandRecyclerViewFully(recyclerAnnouncements);
                })

                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load announcements: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadAnnouncements();
    }
}
