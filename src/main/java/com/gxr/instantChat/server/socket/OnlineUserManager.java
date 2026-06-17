package com.gxr.instantChat.server.socket;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUserManager {

    private static final ConcurrentHashMap<String, ClientHandler> ONLINE_USERS = new ConcurrentHashMap<>();

    public static void addUser(String username, ClientHandler handler) {
        ONLINE_USERS.put(username, handler);
    }

    public static void removeUser(String username) {
        if (username != null) {
            ONLINE_USERS.remove(username);
        }
    }

    public static ClientHandler getHandler(String username) {
        return ONLINE_USERS.get(username);
    }

    public static List<String> getOnlineUsers() {
        return new ArrayList<>(ONLINE_USERS.keySet());
    }

    public static void broadcast(String json) {
        for (ClientHandler handler : ONLINE_USERS.values()) {
            handler.send(json);
        }
    }
}
