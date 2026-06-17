package com.gxr.instantChat.server.socket;


import com.gxr.instantChat.common.JsonUtils;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MessageDispatcher messageDispatcher;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket, MessageDispatcher messageDispatcher) {
        this.socket = socket;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String line;
            while ((line = reader.readLine()) != null) {
                Message message = JsonUtils.fromJson(line, Message.class);
                messageDispatcher.dispatch(message, this);
            }
        } catch (Exception e) {
            System.out.println("客户端异常断开：" + username);
        } finally {
            OnlineUserManager.removeUser(username);
            broadcastOnlineUsers();
            close();
        }
    }

    public void send(String json) {
        if (writer != null) {
            writer.println(json);
        }
    }

    public void send(Message message) {
        send(JsonUtils.toJson(message));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    private void broadcastOnlineUsers() {
        Message message = new Message();
        message.setType(MessageType.ONLINE_USERS);
        message.setOnlineUsers(OnlineUserManager.getOnlineUsers());
        OnlineUserManager.broadcast(JsonUtils.toJson(message));
    }

    private void close() {
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }
}
