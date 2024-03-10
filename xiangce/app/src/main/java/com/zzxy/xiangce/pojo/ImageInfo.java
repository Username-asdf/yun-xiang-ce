package com.zzxy.xiangce.pojo;

import java.io.Serializable;
import java.util.Date;

public class ImageInfo implements Serializable {
    private Long id;

    private Integer uid;

    private Integer fid;

    private Byte isdel;

    private Byte isimg;

    private String imgname;

    private String realname;

    private Long size;

    private Date uploadtime;

    private String tags;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public Byte getIsdel() {
        return isdel;
    }

    public void setIsdel(Byte isdel) {
        this.isdel = isdel;
    }

    public Byte getIsimg() {
        return isimg;
    }

    public void setIsimg(Byte isimg) {
        this.isimg = isimg;
    }

    public String getImgname() {
        return imgname;
    }

    public void setImgname(String imgname) {
        this.imgname = imgname;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getUploadtime() {
        return uploadtime;
    }

    public void setUploadtime(Date uploadtime) {
        this.uploadtime = uploadtime;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", uid=").append(uid);
        sb.append(", fid=").append(fid);
        sb.append(", isdel=").append(isdel);
        sb.append(", isimg=").append(isimg);
        sb.append(", imgname=").append(imgname);
        sb.append(", realname=").append(realname);
        sb.append(", size=").append(size);
        sb.append(", uploadtime=").append(uploadtime);
        sb.append(", tags=").append(tags);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
