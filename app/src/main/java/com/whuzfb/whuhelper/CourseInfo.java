package com.whuzfb.whuhelper;

public class CourseInfo {
    private int id;
    private String courseName;
    private String courseType;
    private String studyType;
    private String college;
    private String teacher;
    private String profession;
    private String credit;
    private String timeLast;
    private String time;
    private String note;
    private String state;
    private String courseID;

    public CourseInfo(int id, String courseName, String courseType, String studyType, String college, String teacher, String profession, String credit, String timeLast, String time, String note, String state, String courseID) {
        this.id = id;
        this.courseName = courseName;
        this.courseType = courseType;
        this.studyType = studyType;
        this.college = college;
        this.teacher = teacher;
        this.profession = profession;
        this.credit = credit;
        this.timeLast = timeLast;
        this.time = time;
        this.note = note;
        this.state = state;
        this.courseID = courseID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getTimeLast() {
        return timeLast;
    }

    public void setTimeLast(String timeLast) {
        this.timeLast = timeLast;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
