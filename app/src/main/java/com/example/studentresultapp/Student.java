package com.example.studentresultapp;

import java.util.Map;

public class Student {
    private String name;
    private String rollNo;
    private String dob;
    private String className;
    private String examName;
    private Map<String, Long> subjects;
    private Long totalMarks;
    private Long maxMarks;
    private Double percentage;
    private String grade;
    private String status;
    private String remarks;
    private Boolean published;
    private String uploadedBy;

    public Student() { }

    public String getName() { return name; }
    public String getRollNo() { return rollNo; }
    public String getDob() { return dob; }
    public String getClassName() { return className; }
    public String getExamName() { return examName; }
    public Map<String, Long> getSubjects() { return subjects; }
    public Long getTotalMarks() { return totalMarks; }
    public Long getMaxMarks() { return maxMarks; }
    public Double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public Boolean getPublished() { return published; }
    public String getUploadedBy() { return uploadedBy; }
}
