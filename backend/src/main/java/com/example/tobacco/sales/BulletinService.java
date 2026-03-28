package com.example.tobacco.sales;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BulletinService {
    private final JdbcTemplate jdbcTemplate;

    public BulletinService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list() {
        return jdbcTemplate.queryForList(
                "select id, title, content, category, status, created_by as createdBy, " +
                "IFNULL(DATE_FORMAT(expired_at,'%Y-%m-%d %H:%i:%s'),'') as expiredAt, " +
                "DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from bulletins order by id desc");
    }

    public void create(String title, String content, String category, String expiredAt, String createdBy) {
        if (expiredAt != null && expiredAt.trim().length() > 0) {
            jdbcTemplate.update("insert into bulletins(title, content, category, expired_at, created_by) values(?,?,?,?,?)",
                    title, content, category, expiredAt, createdBy);
        } else {
            jdbcTemplate.update("insert into bulletins(title, content, category, created_by) values(?,?,?,?)",
                    title, content, category, createdBy);
        }
    }
}
