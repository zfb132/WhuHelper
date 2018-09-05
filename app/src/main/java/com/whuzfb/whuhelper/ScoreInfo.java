package com.whuzfb.whuhelper;

public class ScoreInfo {
    private int id;
    private String courseName;
    private String score;
    private String courseType;
    private String credit;
    private String teacher;
    private String college;
    private String studyType;
    private String year;
    private String term;
    private String courseID;

    public ScoreInfo(int id, String courseName, String score, String courseType, String credit, String teacher, String college, String studyType, String year, String term, String courseID) {
        this.id = id;
        this.courseName = courseName;
        this.score = score;
        this.courseType = courseType;
        this.credit = credit;
        this.teacher = teacher;
        this.college = college;
        this.studyType = studyType;
        this.year = year;
        this.term = term;
        this.courseID = courseID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }
}
