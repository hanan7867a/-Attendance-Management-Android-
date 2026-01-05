package com.example.attandence_managment.grades;

public class AssignmentModel {
    private String assignmentName;
    private String score;
    AssignmentModel(String assignmentName, String score){
        this.assignmentName=assignmentName;
        this.score=score;

    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getScore() {
        return score;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public String getAssignmentName() {
        return assignmentName;
    }
}
