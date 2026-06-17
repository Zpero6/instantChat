package com.gxr.instantChat.server.socket;

import com.gxr.instantChat.common.JsonUtils;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;
import com.gxr.instantChat.server.entity.ChatMessage;
import com.gxr.instantChat.server.service.ChatMessageService;
import com.gxr.instantChat.server.service.UserService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class MessageDispatcher {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserService userService;
    private final ChatMessageService chatMessageService;

    public MessageDispatcher(UserService userService, ChatMessageService chatMessageService) {
        this.userService = userService;
        this.chatMessageService = chatMessageService;
    }

    public void dispatch(Message message, ClientHandler clientHandler) {
        switch (message.getType()) {
            case MessageType.REGISTER:
                handleRegister(message, clientHandler);
                break;
            case MessageType.LOGIN:
                handleLogin(message, clientHandler);
                break;
            case MessageType.PRIVATE_CHAT:
                handlePrivateChat(message, clientHandler);
                break;
            case MessageType.GROUP_CHAT:
                handleGroupChat(message);
                break;
            case MessageType.HISTORY_REQUEST:
                handleHistoryRequest(message, clientHandler);
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

        if (success) {
            clientHandler.setUsername(message.getFrom());
            OnlineUserManager.addUser(message.getFrom(), clientHandler);
            result.setOnlineUsers(OnlineUserManager.getOnlineUsers());
            clientHandler.send(result);
            broadcastOnlineUsers();
        } else {
            clientHandler.send(result);
        }
    }

    private void handlePrivateChat(Message message, ClientHandler clientHandler) {
        chatMessageService.saveMessage(message);

        ClientHandler targetHandler = OnlineUserManager.getHandler(message.getTo());
        if (targetHandler != null) {
            targetHandler.send(message);
        } else {
            sendError(clientHandler, "用户不在线：" + message.getTo());
        }

        clientHandler.send(message);
    }

    private void handleGroupChat(Message message) {
        chatMessageService.saveMessage(message);
        OnlineUserManager.broadcast(JsonUtils.toJson(message));
    }

    private void handleHistoryRequest(Message message, ClientHandler clientHandler) {
        List<ChatMessage> records;
        if ("ALL".equals(message.getTo())) {
            records = chatMessageService.queryGroupHistory();
        } else {
            records = chatMessageService.queryPrivateHistory(message.getFrom(), message.getTo());
        }

        Message result = new Message();
        result.setType(MessageType.HISTORY_RESULT);
        result.setFrom(message.getFrom());
        result.setTo(message.getTo());
        result.setHistoryMessages(toHistoryMessages(records));

        clientHandler.send(result);
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

    private List<Message> toHistoryMessages(List<ChatMessage> records) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage record : records) {
            Message message = new Message();
            message.setType(record.getMsgType());
            message.setFrom(record.getSender());
            message.setTo(record.getReceiver());
            message.setContent(record.getContent());
            message.setFileName(record.getFileName());
            if (record.getSendTime() != null) {
                message.setTime(record.getSendTime().format(TIME_FORMATTER));
            }
            messages.add(message);
        }
        return messages;
    }
}
