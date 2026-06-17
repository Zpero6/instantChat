package com.gxr.instantChat.server;

import com.gxr.instantChat.server.socket.ChatSocketServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@MapperScan("com.gxr.instantChat.server.mapper")
@SpringBootApplication(scanBasePackages = "com.gxr.instantChat.server")
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner startSocketServer(ChatSocketServer chatSocketServer) {
        return args -> chatSocketServer.start();
    }
}
