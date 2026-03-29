package com.example.tobacco.mapper.audit;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AuditMapper {

    @Insert("insert into login_logs(user_id, username, ip, device, status, message) values(#{userId}, #{username}, #{ip}, #{device}, #{status}, #{message})")
    void insertLoginLog(@Param("userId") Long userId,
                        @Param("username") String username,
                        @Param("status") String status,
                        @Param("message") String message,
                        @Param("ip") String ip,
                        @Param("device") String device);

    @Insert("insert into operation_logs(user_id, username, module, action, biz_type, biz_id, detail) values(#{userId}, #{username}, #{module}, #{action}, #{bizType}, #{bizId}, #{detail})")
    void insertOperationLog(@Param("userId") Long userId,
                            @Param("username") String username,
                            @Param("module") String module,
                            @Param("action") String action,
                            @Param("bizType") String bizType,
                            @Param("bizId") Long bizId,
                            @Param("detail") String detail);

    @Insert("insert into trace_records(biz_type, biz_id, order_no, node_code, node_name, operator, remark) values(#{bizType}, #{bizId}, #{orderNo}, #{nodeCode}, #{nodeName}, #{operator}, #{remark})")
    void insertTrace(@Param("bizType") String bizType,
                     @Param("bizId") Long bizId,
                     @Param("orderNo") String orderNo,
                     @Param("nodeCode") String nodeCode,
                     @Param("nodeName") String nodeName,
                     @Param("operator") String operator,
                     @Param("remark") String remark);

    @Insert("insert into abnormal_documents(biz_type, biz_id, order_no, abnormal_type, status, reported_by, detail) values(#{bizType}, #{bizId}, #{orderNo}, #{abnormalType}, #{status}, #{reportedBy}, #{detail})")
    void insertAbnormal(@Param("bizType") String bizType,
                        @Param("bizId") Long bizId,
                        @Param("orderNo") String orderNo,
                        @Param("abnormalType") String abnormalType,
                        @Param("status") String status,
                        @Param("reportedBy") String reportedBy,
                        @Param("detail") String detail);

    @Select("select id, user_id as userId, username, ip, device, status, message, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from login_logs order by id desc")
    List<Map<String, Object>> selectLoginLogs();

    @Select("select id, user_id as userId, username, module, action, biz_type as bizType, biz_id as bizId, detail, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from operation_logs order by id desc")
    List<Map<String, Object>> selectOperationLogs();
}
