package com.example.attandence_managment.announcments;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attandence_managment.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDetailDescription, etPostedBy, etAttachment;
    private Button btnPostAnnouncement;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_announcement);

        // Initialize views (MATCH XML TYPES)
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDetailDescription = findViewById(R.id.etDetailDescription);
        etPostedBy = findViewById(R.id.etPostedBy);
        etAttachment = findViewById(R.id.etAttachment);

        btnPostAnnouncement = findViewById(R.id.btnPostAnnouncement);

        db = FirebaseFirestore.getInstance();

        btnPostAnnouncement.setOnClickListener(v -> postAnnouncement());
    }

    private void postAnnouncement() {

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String detailDesc = etDetailDescription.getText().toString().trim();
        String postedBy = etPostedBy.getText().toString().trim();
        String attachment = etAttachment.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || detailDesc.isEmpty() || postedBy.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("description", description);
        announcement.put("detaildesc", detailDesc);
        announcement.put("postedBy", postedBy);
        announcement.put("fileAttachment", attachment);
        announcement.put("image", ""); // optional
        announcement.put("date", com.google.firebase.Timestamp.now());

        db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Announcement posted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
