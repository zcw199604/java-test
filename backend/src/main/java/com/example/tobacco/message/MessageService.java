package com.example.tobacco.message;

import com.example.tobacco.mapper.message.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public void createMessage(Long userId, String title, String content, String messageType, String bizType, Long bizId) {
        messageMapper.insertMessage(userId, title, content, messageType, bizType, bizId);
    }

    public List<Map<String, Object>> list(Long userId) {
        return messageMapper.selectMessages(userId);
    }

    public void read(Long id) {
        messageMapper.markRead(id);
    }
}
