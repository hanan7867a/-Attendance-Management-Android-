package com.example.attandence_managment.announcments;



public class AnnouncementModel {

    private String id;
    private String title;
    private String description;
    private String image;
    private String fileAttachment;
    private String postedBy;
    private String date;

    public AnnouncementModel(String id, String title, String description,
                             String image, String fileAttachment,
                             String postedBy, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.fileAttachment = fileAttachment;
        this.postedBy = postedBy;
        this.date = date;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public String getFileAttachment() { return fileAttachment; }
    public String getPostedBy() { return postedBy; }
    public String getDate() { return date; }
}
