package com.gxr.instantChat.server.socket;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;

@Component
public class ChatSocketServer {

    @Value("${chat.socket-port}")
    private int socketPort;

    private final MessageDispatcher messageDispatcher;

    public ChatSocketServer(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
                System.out.println("Socket 服务端启动成功，端口：" + socketPort);

                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, messageDispatcher);
                    new Thread(clientHandler).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
