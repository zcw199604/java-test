package com.example.tobacco.mapper.message;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {

    @Insert("insert into messages(user_id, title, content, message_type, biz_type, biz_id, is_read) values(#{userId}, #{title}, #{content}, #{messageType}, #{bizType}, #{bizId}, 0)")
    void insertMessage(@Param("userId") Long userId,
                       @Param("title") String title,
                       @Param("content") String content,
                       @Param("messageType") String messageType,
                       @Param("bizType") String bizType,
                       @Param("bizId") Long bizId);

    @Select("select id, user_id as userId, title, content, message_type as messageType, biz_type as bizType, biz_id as bizId, is_read as isRead, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(read_at,'%Y-%m-%d %H:%i:%s'),'') as readAt from messages where user_id=#{userId} or user_id is null order by id desc")
    List<Map<String, Object>> selectMessages(@Param("userId") Long userId);

    @Update("update messages set is_read=1, read_at=now() where id=#{id}")
    void markRead(@Param("id") Long id);
}
