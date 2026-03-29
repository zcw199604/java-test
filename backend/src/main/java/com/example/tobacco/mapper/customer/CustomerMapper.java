package com.example.tobacco.mapper.customer;

import com.example.tobacco.model.CustomerItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @SelectProvider(type = CustomerSqlProvider.class, method = "buildListSql")
    List<CustomerItem> list(@Param("keyword") String keyword,
                            @Param("keywordLike") String keywordLike,
                            @Param("status") String status);

    @Select("select id, name, contact_name as contactName, contact_phone as contactPhone, address, status from customers where id=#{id}")
    CustomerItem detail(@Param("id") Long id);

    @Insert("insert into customers(name, contact_name, contact_phone, address, status) values(#{name}, #{contactName}, #{contactPhone}, #{address}, #{status})")
    void insert(@Param("name") String name,
                @Param("contactName") String contactName,
                @Param("contactPhone") String contactPhone,
                @Param("address") String address,
                @Param("status") String status);

    @Update("update customers set name=#{name}, contact_name=#{contactName}, contact_phone=#{contactPhone}, address=#{address}, status=#{status} where id=#{id}")
    void update(@Param("id") Long id,
                @Param("name") String name,
                @Param("contactName") String contactName,
                @Param("contactPhone") String contactPhone,
                @Param("address") String address,
                @Param("status") String status);

    @Update("update customers set status='DISABLED' where id=#{id}")
    void disable(@Param("id") Long id);
}
