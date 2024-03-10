package com.zzxy.xiangce.pojo;

public class ResponseResult {
    private int id;
    private String userName;
    private int subjectInfoId;
    private String subjectInfoName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSubjectInfoId() {
        return subjectInfoId;
    }

    public void setSubjectInfoId(int subjectInfoId) {
        this.subjectInfoId = subjectInfoId;
    }

    public String getSubjectInfoName() {
        return subjectInfoName;
    }

    public void setSubjectInfoName(String subjectInfoName) {
        this.subjectInfoName = subjectInfoName;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", subjectInfoId=" + subjectInfoId +
                ", subjectInfoName='" + subjectInfoName + '\'' +
                '}';
    }
}
