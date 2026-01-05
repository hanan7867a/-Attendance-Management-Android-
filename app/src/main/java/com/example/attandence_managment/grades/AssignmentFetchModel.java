package com.example.attandence_managment.grades;

import com.google.firebase.firestore.DocumentId;

public class AssignmentFetchModel {

    private String title;
    private String description;
    private String teacherId;
    private String url;
    private long createdAt;
    private int totalMarks;
    private boolean submitted = false;

    @DocumentId
    private String assignmentId; // Firestore document ID

    // Required empty constructor for Firestore
    public AssignmentFetchModel() {}

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getUrl() {
        return url;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    // Optional: setter for URL if you want to mark submitted file
    public void setUrl(String url) {
        this.url = url;
    }
    public boolean isSubmitted() { return submitted; } // NEW
    public void setSubmitted(boolean submitted) { this.submitted = submitted; } // NEW
}
