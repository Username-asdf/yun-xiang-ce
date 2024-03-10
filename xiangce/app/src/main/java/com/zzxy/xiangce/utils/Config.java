package com.zzxy.xiangce.utils;

public interface Config {
    String url = "http://192.168.0.105";
    String loginUrl = url+"/login";
    String registerUrl = url+"/register";
    String updatePassword = url +"/up";
    //获取文件夹下所有文件夹及图片
    //pageSize分页大小
    //pageNum页数
    //fid 父文件夹ID
    String getFolderFile = url+"/gij";
    //imgName 图片名称
    String getSmallImg = url + "/gsi";
    String getImg = url + "/gi";
    String uploadImageUrl = url+"/upload";
    String delImageUrl = url+"/di"; //id
    String delImageFolder = url +"/df"; //fid
    String downloadUrl = url+"/downloadFile"; //filename
}
