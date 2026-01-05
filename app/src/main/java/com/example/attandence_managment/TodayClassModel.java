package com.example.attandence_managment;

public class TodayClassModel {

    public String courseName;
    public String section;
    public String startTime;
    public String endTime;
    public String venue;
    public String teacherName;


    public TodayClassModel() {}

    public TodayClassModel(String courseName, String section,
                           String startTime, String endTime, String venue,String teacherName) {
        this.courseName = courseName;
        this.section = section;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.teacherName=teacherName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getSection() {
        return section;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
}
