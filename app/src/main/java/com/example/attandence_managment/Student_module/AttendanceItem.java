package com.example.attandence_managment.Student_module;

public class AttendanceItem {
    private String date;
    private String subject;
    private String status;

    public AttendanceItem(String date, String subject, String status) {
        this.date = date;
        this.subject = subject;
        this.status = status;
    }

    public String getDate() { return date; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
}
