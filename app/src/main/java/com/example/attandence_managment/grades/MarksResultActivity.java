package com.example.attandence_managment.grades;

import static android.content.Intent.getIntent;

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
import com.example.attandence_managment.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import com.google.firebase.firestore.DocumentReference;



public class MarksResultActivity extends AppCompatActivity {

    private static final String TAG = "MarksResultActivity";

    private RecyclerView rvMarks;
    TextView studentName,studentEmail;
    private ArrayList<MarkItem> marksList;
    private MarksAdapter adapter;
    private FirebaseFirestore db;

    private String studentId;
    private String subjectId;
    ImageView profileImage;
    private String fieldName,name,email,profilePic; // assignments, quizzes, midsmarks, finalmarks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_marks); // Can reuse same layout

        // Get data from Intent
        studentId = getIntent().getStringExtra("studentId");
        subjectId = getIntent().getStringExtra("subjectId");
        fieldName = getIntent().getStringExtra("fieldName");
        name=getIntent().getStringExtra("name");
        email=getIntent().getStringExtra("email");
        profilePic=getIntent().getStringExtra("image");
        profileImage=findViewById(R.id.profile_image);




        Log.d(TAG, "Fetching marks for Student: " + studentId + ", Subject: " + subjectId + ", Field: " + fieldName);

        rvMarks = findViewById(R.id.rv_assignment_marks);
        marksList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);
      studentName=findViewById(R.id.name_of_student);
      studentEmail=findViewById(R.id.std_email);

      studentName.setText(name);
      studentEmail.setText(email);
        Glide.with(this)
                .load(profilePic)
                .placeholder(R.drawable.profilepicture)
                .circleCrop()
                .into(profileImage);



        // Setup RecyclerView
        rvMarks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MarksAdapter(marksList);
        rvMarks.setAdapter(adapter);


        fetchMarks( fieldName);
    }

    private void fetchMarks(String fieldName) {
        Log.d(TAG, "Fetching category: " + fieldName);


        String mapField;

        switch (fieldName) {
            case "assignments":
                mapField = "assignmentsmarks";
                break;
            case "quizzes":
                mapField = "quizesmrks";
                break;
            case "mids":
                mapField = "midsmarks";
                break;
            case "finals":
                mapField = "finalmarks";
                break;
            default:
                Toast.makeText(this, "Invalid marks category", Toast.LENGTH_SHORT).show();
                return;
        }

        db.collection("courses")
                .document(subjectId)
                .collection("marks")
                .document(fieldName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "No marks found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> marksMap =
                            (Map<String, Object>) documentSnapshot.get(mapField);

                    marksList.clear();

                    if (marksMap != null && !marksMap.isEmpty()) {
                        for (Map.Entry<String, Object> entry :
                                new TreeMap<>(marksMap).entrySet()) {

                            String name = prettifyKey(entry.getKey());
                            String score = convertToScore(
                                    entry.getValue(),
                                    determineSuffix(fieldName)
                            );

                            marksList.add(new MarkItem(name, score));
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching marks", e);
                    Toast.makeText(this, "Failed to load marks", Toast.LENGTH_SHORT).show();
                });
    }



    // Decide suffix based on field type
    private String determineSuffix(String fieldName) {
        switch (fieldName) {
            case "assignments":
            case "quizzes":
                return "/20";

            case "mids":
            case "finals":
                return "/50";

            default:
                return "";
        }
    }


    // Make key prettier: assignment_1 → Assignment 1
    private String prettifyKey(String key) {
        key = key.replace("_", " ").replace("-", " ").replaceAll("\\s+", " ").trim();
        if (key.isEmpty()) return "";
        return key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
    }

    // Convert value to string
    private String convertToScore(Object value, String suffix) {
        if (value == null) return "0" + suffix;
        try {
            return String.valueOf(value) + suffix;
        } catch (Exception e) {
            return "N/A";
        }
    }

    // ========== Model Class ==========
    public static class MarkItem {
        private String name;
        private String score;

        public MarkItem(String name, String score) {
            this.name = name;
            this.score = score;
        }

        public String getName() { return name; }
        public String getScore() { return score; }
    }

    // ========== RecyclerView Adapter ==========
    private class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.ViewHolder> {

        private final ArrayList<MarkItem> list;

        public MarksAdapter(ArrayList<MarkItem> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_assignment_mark, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MarkItem item = list.get(position);
            holder.tvName.setText(item.getName());
            holder.tvMarks.setText(item.getScore());
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMarks;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_assignment_name);
                tvMarks = itemView.findViewById(R.id.tv_assignment_score);
            }
        }
    }

}
