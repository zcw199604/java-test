package com.example.tobacco.message;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    private final JdbcTemplate jdbcTemplate;

    public MessageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createMessage(Long userId, String title, String content, String messageType, String bizType, Long bizId) {
        jdbcTemplate.update("insert into messages(user_id, title, content, message_type, biz_type, biz_id, is_read) values(?,?,?,?,?,?,0)",
                userId, title, content, messageType, bizType, bizId);
    }

    public List<Map<String, Object>> list(Long userId) {
        return jdbcTemplate.queryForList("select id, user_id as userId, title, content, message_type as messageType, biz_type as bizType, biz_id as bizId, is_read as isRead, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(read_at,'%Y-%m-%d %H:%i:%s'),'') as readAt from messages where user_id=? or user_id is null order by id desc", userId);
    }

    public void read(Long id) {
        jdbcTemplate.update("update messages set is_read=1, read_at=now() where id=?", id);
    }
}
