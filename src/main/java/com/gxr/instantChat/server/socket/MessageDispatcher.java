package com.gxr.instantChat.server.socket;


import com.gxr.instantChat.common.JsonUtils;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;
import com.gxr.instantChat.server.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class MessageDispatcher {

    private final UserService userService;

    public MessageDispatcher(UserService userService) {
        this.userService = userService;
    }

    public void dispatch(Message message, ClientHandler clientHandler) {
        switch (message.getType()) {
            case MessageType.REGISTER:
                handleRegister(message, clientHandler);
                break;
            case MessageType.LOGIN:
                handleLogin(message, clientHandler);
                break;
            case MessageType.LOGOUT:
                handleLogout(clientHandler);
                break;
            default:
                sendError(clientHandler, "暂不支持的消息类型：" + message.getType());
        }
    }

    private void handleRegister(Message message, ClientHandler clientHandler) {
        boolean success = userService.register(message.getFrom(), message.getContent());

        Message result = new Message();
        result.setType(MessageType.REGISTER_RESULT);
        result.setSuccess(success);
        result.setReason(success ? "注册成功" : "用户名已存在");

        clientHandler.send(result);
    }

    private void handleLogin(Message message, ClientHandler clientHandler) {
        boolean success = userService.login(message.getFrom(), message.getContent());

        Message result = new Message();
        result.setType(MessageType.LOGIN_RESULT);
        result.setSuccess(success);
        result.setReason(success ? "登录成功" : "用户名或密码错误");

        clientHandler.send(result);

        if (success) {
            clientHandler.setUsername(message.getFrom());
            OnlineUserManager.addUser(message.getFrom(), clientHandler);
            broadcastOnlineUsers();
        }
    }

    private void handleLogout(ClientHandler clientHandler) {
        OnlineUserManager.removeUser(clientHandler.getUsername());
        broadcastOnlineUsers();
    }

    private void broadcastOnlineUsers() {
        Message message = new Message();
        message.setType(MessageType.ONLINE_USERS);
        message.setOnlineUsers(OnlineUserManager.getOnlineUsers());

        OnlineUserManager.broadcast(JsonUtils.toJson(message));
    }

    private void sendError(ClientHandler clientHandler, String reason) {
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSuccess(false);
        message.setReason(reason);

        clientHandler.send(message);
    }
}
