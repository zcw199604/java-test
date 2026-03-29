package com.example.tobacco.audit;

import com.example.tobacco.mapper.audit.AuditMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuditService {
    private final AuditMapper auditMapper;

    public AuditService(AuditMapper auditMapper) {
        this.auditMapper = auditMapper;
    }

    public void logLogin(Long userId, String username, String status, String message, String ip, String device) {
        auditMapper.insertLoginLog(userId, username, status, message, ip, device);
    }

    public void logOperation(Long userId, String username, String module, String action, String bizType, Long bizId, String detail) {
        auditMapper.insertOperationLog(userId, username, module, action, bizType, bizId, detail);
    }

    public void trace(String bizType, Long bizId, String orderNo, String nodeCode, String nodeName, String operator, String remark) {
        auditMapper.insertTrace(bizType, bizId, orderNo, nodeCode, nodeName, operator, remark);
    }

    public void abnormal(String bizType, Long bizId, String orderNo, String abnormalType, String status, String reportedBy, String detail) {
        auditMapper.insertAbnormal(bizType, bizId, orderNo, abnormalType, status, reportedBy, detail);
    }

    public List<Map<String, Object>> loginLogs() {
        return auditMapper.selectLoginLogs();
    }

    public List<Map<String, Object>> operationLogs() {
        return auditMapper.selectOperationLogs();
    }
}
