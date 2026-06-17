package com.gxr.instantChat.client.socket;


import com.gxr.instantChat.common.JsonUtils;
import com.gxr.instantChat.common.Message;

import java.io.BufferedReader;
import java.util.function.Consumer;

public class ServerListener implements Runnable {

    private final BufferedReader reader;
    private final Consumer<Message> messageConsumer;

    public ServerListener(BufferedReader reader, Consumer<Message> messageConsumer) {
        this.reader = reader;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                Message message = JsonUtils.fromJson(line, Message.class);
                messageConsumer.accept(message);
            }
        } catch (Exception e) {
            System.out.println("与服务器连接断开");
        }
    }
}
