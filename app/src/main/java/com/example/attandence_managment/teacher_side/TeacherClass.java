package com.example.attandence_managment.teacher_side;
public class TeacherClass {
    private String courseId;
    private String courseName;
    private String section;
    private String venue;
    private String startTime;
    private String endTime;
    private int studentCount;

    public TeacherClass() {} // Empty constructor for Firestore

    public TeacherClass(String courseId, String courseName, String section,
                        String venue, String startTime, String endTime, int studentCount) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.section = section;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.studentCount = studentCount;
    }

    // Getters
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getSection() { return section; }
    public String getVenue() { return venue; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getStudentCount() { return studentCount; }
}
