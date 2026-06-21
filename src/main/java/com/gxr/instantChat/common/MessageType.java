package com.gxr.instantChat.common;


public class MessageType {

    public static final String REGISTER = "REGISTER";
    public static final String REGISTER_RESULT = "REGISTER_RESULT";

    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_RESULT = "LOGIN_RESULT";

    public static final String ONLINE_USERS = "ONLINE_USERS";

    public static final String PRIVATE_CHAT = "PRIVATE_CHAT";
    public static final String GROUP_CHAT = "GROUP_CHAT";

    public static final String HISTORY_REQUEST = "HISTORY_REQUEST";
    public static final String HISTORY_RESULT = "HISTORY_RESULT";

    // 关键字搜索请求 , 搜索结果  两种消息类型
    public static final String SEARCH_REQUEST = "SEARCH_REQUEST";
    public static final String SEARCH_RESULT = "SEARCH_RESULT";

// 读取已读未读状态
    public static final String READ_RECEIPT = "READ_RECEIPT";

    public static final String FILE = "FILE";

    public static final String LOGOUT = "LOGOUT";
    public static final String ERROR = "ERROR";
}
