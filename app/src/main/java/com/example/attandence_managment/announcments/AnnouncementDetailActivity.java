package com.example.attandence_managment.announcments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class AnnouncementDetailActivity extends AppCompatActivity {

    private static final String TAG = "AnnouncementDetail";

    private TextView tvTitle, tvMeta, tvDescription;
    private ImageView ivAnnouncementImage;
    private LinearLayout btnAttachment;

    private FirebaseFirestore db;
    private String announcementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.announcment_detail_screen);

        Log.d(TAG, "onCreate: started");

        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvDescription = findViewById(R.id.tvDescription);
        ivAnnouncementImage = findViewById(R.id.announcementImage);
        btnAttachment = findViewById(R.id.btnAttachment);

        db = FirebaseFirestore.getInstance();

        // Get announcementId from Intent
        if (getIntent() != null) {
            announcementId = getIntent().getStringExtra("announcementId");
            Log.d(TAG, "onCreate: received announcementId = " + announcementId);
        }

        if (announcementId != null) {
            fetchAnnouncementDetails(announcementId);
        } else {
            Log.e(TAG, "onCreate: announcementId is null");
            Toast.makeText(this, "Announcement ID missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAnnouncementDetails(String id) {
        Log.d(TAG, "fetchAnnouncementDetails: fetching announcement with id = " + id);

        db.collection("announcements").document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "fetchAnnouncementDetails: onSuccess called");

                    if (!doc.exists()) {
                        Log.e(TAG, "fetchAnnouncementDetails: document does not exist");
                        Toast.makeText(this, "Announcement not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        String title = doc.getString("title");
                        String description = doc.getString("detaildesc");
                        String postedBy = doc.getString("postedBy");
                        String date = "";
                        if (doc.getTimestamp("date") != null) {
                            date = new java.text.SimpleDateFormat(
                                    "dd MMM yyyy", java.util.Locale.getDefault()
                            ).format(doc.getTimestamp("date").toDate());
                        }
                        String imageUrl = doc.getString("image");
                        String fileAttachment = doc.getString("fileAttachment");

                        Log.d(TAG, "fetchAnnouncementDetails: title=" + title + ", postedBy=" + postedBy + ", date=" + date);

                        tvTitle.setText(title);
                        tvDescription.setText(description);
                        tvMeta.setText(date + " • Posted by " + postedBy);

                        // Load image if exists
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d(TAG, "fetchAnnouncementDetails: loading image " + imageUrl);
                            ivAnnouncementImage.setVisibility(View.VISIBLE);
                            Glide.with(this).load(imageUrl).into(ivAnnouncementImage);
                        } else {
                            Log.d(TAG, "fetchAnnouncementDetails: no image found");
                            ivAnnouncementImage.setVisibility(View.GONE);
                        }

                        // Handle attachment button
                        if (fileAttachment != null && !fileAttachment.isEmpty()) {
                            Log.d(TAG, "fetchAnnouncementDetails: attachment exists " + fileAttachment);
                            btnAttachment.setVisibility(View.VISIBLE);
                            btnAttachment.setOnClickListener(v -> {
                                Log.d(TAG, "Attachment clicked, opening URL: " + fileAttachment);
                                try {
                                    Uri uri = Uri.parse(fileAttachment);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    // Verify that there is an app to handle this intent
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(this, "No app available to open this link", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error opening attachment URL", e);
                                    Toast.makeText(this, "Invalid attachment link", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.d(TAG, "fetchAnnouncementDetails: no attachment");
                            btnAttachment.setVisibility(View.GONE);
                        }


                    } catch (Exception e) {
                        Log.e(TAG, "fetchAnnouncementDetails: Exception while setting data", e);
                        Toast.makeText(this, "Error loading announcement", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchAnnouncementDetails: Failed to load announcement", e);
                    Toast.makeText(this, "Failed to load announcement: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
