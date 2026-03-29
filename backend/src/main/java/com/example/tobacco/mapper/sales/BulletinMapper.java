package com.example.tobacco.mapper.sales;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BulletinMapper {

    @Select("select id, title, content, category, status, created_by as createdBy, IFNULL(DATE_FORMAT(expired_at,'%Y-%m-%d %H:%i:%s'),'') as expiredAt, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from bulletins order by id desc")
    List<Map<String, Object>> selectBulletins();

    @Insert("insert into bulletins(title, content, category, expired_at, created_by) values(#{title}, #{content}, #{category}, #{expiredAt}, #{createdBy})")
    void insertBulletinWithExpireAt(@Param("title") String title,
                                    @Param("content") String content,
                                    @Param("category") String category,
                                    @Param("expiredAt") String expiredAt,
                                    @Param("createdBy") String createdBy);

    @Insert("insert into bulletins(title, content, category, created_by) values(#{title}, #{content}, #{category}, #{createdBy})")
    void insertBulletin(@Param("title") String title,
                        @Param("content") String content,
                        @Param("category") String category,
                        @Param("createdBy") String createdBy);
}
