package com.gxr.instantChat.server.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxr.instantChat.server.entity.User;
import com.gxr.instantChat.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean register(String username, String password) {
        User exists = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );

        if (exists != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(username);
        user.setCreateTime(LocalDateTime.now());

        return userMapper.insert(user) > 0;
    }

    public boolean login(String username, String password) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("username", username)
                        .eq("password", password)
        );

        return user != null;
    }
}
