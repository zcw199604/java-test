package com.example.tobacco.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuditService {
    private final JdbcTemplate jdbcTemplate;

    public AuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void logLogin(Long userId, String username, String status, String message, String ip, String device) {
        jdbcTemplate.update("insert into login_logs(user_id, username, ip, device, status, message) values(?,?,?,?,?,?)",
                userId, username, ip, device, status, message);
    }

    public void logOperation(Long userId, String username, String module, String action, String bizType, Long bizId, String detail) {
        jdbcTemplate.update("insert into operation_logs(user_id, username, module, action, biz_type, biz_id, detail) values(?,?,?,?,?,?,?)",
                userId, username, module, action, bizType, bizId, detail);
    }

    public void trace(String bizType, Long bizId, String orderNo, String nodeCode, String nodeName, String operator, String remark) {
        jdbcTemplate.update("insert into trace_records(biz_type, biz_id, order_no, node_code, node_name, operator, remark) values(?,?,?,?,?,?,?)",
                bizType, bizId, orderNo, nodeCode, nodeName, operator, remark);
    }

    public void abnormal(String bizType, Long bizId, String orderNo, String abnormalType, String status, String reportedBy, String detail) {
        jdbcTemplate.update("insert into abnormal_documents(biz_type, biz_id, order_no, abnormal_type, status, reported_by, detail) values(?,?,?,?,?,?,?)",
                bizType, bizId, orderNo, abnormalType, status, reportedBy, detail);
    }

    public List<Map<String, Object>> loginLogs() {
        return jdbcTemplate.queryForList("select id, user_id as userId, username, ip, device, status, message, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from login_logs order by id desc");
    }

    public List<Map<String, Object>> operationLogs() {
        return jdbcTemplate.queryForList("select id, user_id as userId, username, module, action, biz_type as bizType, biz_id as bizId, detail, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from operation_logs order by id desc");
    }
}
