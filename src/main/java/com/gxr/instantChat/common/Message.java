package com.gxr.instantChat.common;


import lombok.Data;

import java.util.List;

@Data
public class Message {
    /**
     * type            消息类型
     * from            发送者
     * to              接收者
     * content         文本内容，也可以放 Base64 文件内容
     * fileName        文件名
     * fileSize        文件大小
     * success         登录/注册是否成功
     * reason          失败原因
     * onlineUsers     在线用户列表
     * historyMessages 历史聊天记录
     * */


    private String type;

    private String from;

    private String to;

    private String content;

    private String fileName;

    private Long fileSize;

    private String time;

    private Boolean success;

    private String reason;

    private List<String> onlineUsers;

    private List<Message> historyMessages;
}
