// StudentItem.java
package com.example.attandence_managment.teacher_side;

public class StudentItem {
    public String studentId;
    public String name;
    public boolean present;
    public String imgurl;

    public StudentItem(String studentId, String name, boolean present,String imgurl) {
        this.studentId = studentId;
        this.name = name;
        this.present = present;
        this.imgurl=imgurl;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    public String getImgurl() {
        return imgurl;
    }
    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }
}
