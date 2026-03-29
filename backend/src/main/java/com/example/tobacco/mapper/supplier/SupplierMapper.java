package com.example.tobacco.mapper.supplier;

import com.example.tobacco.model.SupplierItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SupplierMapper {

    @SelectProvider(type = SupplierSqlProvider.class, method = "buildListSql")
    List<SupplierItem> list(@Param("keyword") String keyword,
                            @Param("keywordLike") String keywordLike,
                            @Param("status") String status);

    @Insert("insert into suppliers(name, contact_name, contact_phone, address, status) values(#{name}, #{contactName}, #{contactPhone}, #{address}, 'ENABLED')")
    void insert(@Param("name") String name,
                @Param("contactName") String contactName,
                @Param("contactPhone") String contactPhone,
                @Param("address") String address);

    @Update("update suppliers set name=#{name}, contact_name=#{contactName}, contact_phone=#{contactPhone}, address=#{address} where id=#{id}")
    void update(@Param("id") Long id,
                @Param("name") String name,
                @Param("contactName") String contactName,
                @Param("contactPhone") String contactPhone,
                @Param("address") String address);

    @Update("update suppliers set status='DISABLED' where id=#{id}")
    void disable(@Param("id") Long id);
}
