package com.zzxy.xiangce;


import com.zzxy.xiangce.pojo.Users;

public interface TMessage {
    int SEND_DATA = 1, CHANGE_UI=2, CHANGE_AND_DATA=3;
    void sendMessage(int type,Object data);
}
