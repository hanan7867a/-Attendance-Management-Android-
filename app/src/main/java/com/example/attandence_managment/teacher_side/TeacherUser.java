// File: TeacherUser.java
package com.example.attandence_managment.teacher_side;

import java.util.ArrayList;
import java.util.List;

public class TeacherUser {
    private String name;
    private String email;
    private String profilePic;
    private String role = "teacher";
    private String studentId;        // used as teacher ID
    private List<String> enrolledCourses = new ArrayList<>();

    // Required empty constructor for Firestore
    public TeacherUser() {}

    public TeacherUser(String name, String email, String profilePic, String studentId) {
        this.name = name;
        this.email = email;
        this.profilePic = profilePic;
        this.studentId = studentId;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public List<String> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public void addEnrolledCourse(String courseCode) {
        if (!enrolledCourses.contains(courseCode)) {
            enrolledCourses.add(courseCode);
        }
    }
}