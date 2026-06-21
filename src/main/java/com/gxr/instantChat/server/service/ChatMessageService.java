package com.gxr.instantChat.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;
import com.gxr.instantChat.server.entity.ChatMessage;
import com.gxr.instantChat.server.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public void saveMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMsgType(message.getType());
        chatMessage.setSender(message.getFrom());
        chatMessage.setReceiver(message.getTo());
        chatMessage.setContent(message.getContent());
        chatMessage.setFileName(message.getFileName());
        chatMessage.setSendTime(LocalDateTime.now());

        chatMessageMapper.insert(chatMessage);
    }

    public List<ChatMessage> queryPrivateHistory(String user1, String user2) {
        return chatMessageMapper.selectList(
                new QueryWrapper<ChatMessage>()
                        .and(wrapper -> wrapper
                                .eq("sender", user1).eq("receiver", user2)
                                .or()
                                .eq("sender", user2).eq("receiver", user1)
                        )
                        .in("msg_type", MessageType.PRIVATE_CHAT, MessageType.FILE)
                        .orderByAsc("send_time")
        );
    }

    public List<ChatMessage> queryGroupHistory() {
        return chatMessageMapper.selectList(
                new QueryWrapper<ChatMessage>()
                        .and(wrapper -> wrapper
                                .eq("msg_type", MessageType.GROUP_CHAT)
                                .or(groupFile -> groupFile
                                        .eq("msg_type", MessageType.FILE)
                                        .eq("receiver", "ALL")
                                )
                        )
                        .orderByAsc("send_time")
        );
    }

    public List<ChatMessage> searchPrivateHistory(String user1, String user2, String keyword) {
        String searchText = keyword == null ? "" : keyword.trim();
        if (searchText.isEmpty()) {
            return List.of();
        }

        return chatMessageMapper.selectList(
                new QueryWrapper<ChatMessage>()
                        .and(wrapper -> wrapper
                                .eq("sender", user1).eq("receiver", user2)
                                .or()
                                .eq("sender", user2).eq("receiver", user1)
                        )
                        .in("msg_type", MessageType.PRIVATE_CHAT, MessageType.FILE)
                        .and(wrapper -> wrapper
                                .like("content", searchText)
                                .or()
                                .like("file_name", searchText)
                        )
                        .orderByAsc("send_time")
        );
    }

    public List<ChatMessage> searchGroupHistory(String keyword) {
        String searchText = keyword == null ? "" : keyword.trim();
        if (searchText.isEmpty()) {
            return List.of();
        }

        return chatMessageMapper.selectList(
                new QueryWrapper<ChatMessage>()
                        .and(wrapper -> wrapper
                                .eq("msg_type", MessageType.GROUP_CHAT)
                                .or(groupFile -> groupFile
                                        .eq("msg_type", MessageType.FILE)
                                        .eq("receiver", "ALL")
                                )
                        )
                        .and(wrapper -> wrapper
                                .like("content", searchText)
                                .or()
                                .like("file_name", searchText)
                        )
                        .orderByAsc("send_time")
        );
    }
}
