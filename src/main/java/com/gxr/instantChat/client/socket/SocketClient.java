package com.gxr.instantChat.client.socket;


import com.gxr.instantChat.common.JsonUtils;
import com.gxr.instantChat.common.Message;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Consumer<Message> messageConsumer;

    public void connect(String host, int port, Consumer<Message> messageConsumer) throws IOException {
        this.messageConsumer = messageConsumer;

        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

        new Thread(new ServerListener(reader, this.messageConsumer)).start();
    }

    public void send(Message message) {
        if (writer == null) {
            throw new IllegalStateException("尚未连接服务器");
        }
        writer.println(JsonUtils.toJson(message));
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && writer != null;
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }
}
