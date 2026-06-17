package com.gxr.instantChat.server;

import com.gxr.instantChat.server.service.UserService;
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
    public CommandLineRunner testUserService(UserService userService) {
        return args -> {
            boolean registerResult = userService.register("test", "123456");
            boolean loginResult = userService.login("test", "123456");

            System.out.println("注册结果：" + registerResult);
            System.out.println("登录结果：" + loginResult);
        };
    }
}
