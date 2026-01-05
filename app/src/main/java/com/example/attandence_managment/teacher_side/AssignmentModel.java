package com.example.attandence_managment.teacher_side;

public class AssignmentModel {

    public String title;
    public String description;
    public int totalMarks;
    public String url;
    public String teacherId;
    public long createdAt;


    // Firestore needs empty constructor
    public AssignmentModel() {}

    public AssignmentModel(String title, String description, int totalMarks,
                           String url, String teacherId, long createdAt) {
        this.title = title;
        this.description = description;
        this.totalMarks = totalMarks;
        this.url = url;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }


}
