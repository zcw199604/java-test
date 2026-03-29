package com.example.tobacco.mapper.system;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemMapper {

    @Select("select u.id, u.username, u.real_name as realName, u.role_code as roleCode, r.name as roleName, u.status, DATE_FORMAT(u.created_at, '%Y-%m-%d %H:%i:%s') as createdAt, (select scope_type from user_data_scopes s where s.user_id=u.id order by s.id desc limit 1) as scopeType, (select scope_value from user_data_scopes s where s.user_id=u.id order by s.id desc limit 1) as scopeValue from users u left join roles r on u.role_code = r.code order by u.id")
    List<com.example.tobacco.model.UserProfile> selectUsers();

    @Select("select id, username, real_name as realName, role_code as roleCode, status from users where id=#{id}")
    Map<String, Object> selectUserDetail(@Param("id") Long id);

    @Insert("insert into users(username, password, real_name, role_code, status) values(#{username}, #{password}, #{realName}, #{roleCode}, #{status})")
    void insertUser(@Param("username") String username,
                    @Param("password") String password,
                    @Param("realName") String realName,
                    @Param("roleCode") String roleCode,
                    @Param("status") String status);

    @Select("select id from users where username=#{username}")
    Long selectUserIdByUsername(@Param("username") String username);

    @Select("select username, real_name as realName, role_code as roleCode, status from users where id=#{id}")
    Map<String, Object> selectUserBaseById(@Param("id") Long id);

    @Update("update users set real_name=#{realName}, role_code=#{roleCode}, status=#{status} where id=#{id}")
    void updateUser(@Param("id") Long id,
                    @Param("realName") String realName,
                    @Param("roleCode") String roleCode,
                    @Param("status") String status);

    @Update("update users set password=#{password} where id=#{id}")
    void updateUserPasswordById(@Param("id") Long id, @Param("password") String password);

    @Select("select role_code from users where id=#{id}")
    String selectUserRoleCodeById(@Param("id") Long id);

    @Update("update users set status=#{status} where id=#{id}")
    void updateUserStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("delete from user_data_scopes where user_id=#{userId}")
    void deleteUserDataScopes(@Param("userId") Long userId);

    @Insert("insert into user_data_scopes(user_id, scope_type, scope_value) values(#{userId}, #{scopeType}, #{scopeValue})")
    void insertUserDataScope(@Param("userId") Long userId,
                             @Param("scopeType") String scopeType,
                             @Param("scopeValue") String scopeValue);

    @Delete("delete from user_sessions where user_id=#{userId}")
    void deleteUserSessions(@Param("userId") Long userId);

    @Delete("delete from users where id=#{id}")
    void deleteUser(@Param("id") Long id);

    @Select("select id, config_key as configKey, config_value as configValue, remark, DATE_FORMAT(updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from system_configs order by id")
    List<Map<String, Object>> selectConfigs();

    @Update("update system_configs set config_value=#{configValue}, remark=#{remark}, updated_at=now() where config_key=#{configKey}")
    int updateConfig(@Param("configKey") String configKey,
                     @Param("configValue") String configValue,
                     @Param("remark") String remark);

    @Insert("insert into system_configs(config_key, config_value, remark) values(#{configKey}, #{configValue}, #{remark})")
    void insertConfig(@Param("configKey") String configKey,
                      @Param("configValue") String configValue,
                      @Param("remark") String remark);

    @Select("select id, username, real_name as realName, role_code as roleCode, status from users where username=#{username}")
    Map<String, Object> selectProfileByUsername(@Param("username") String username);

    @Select("select scope_type as scopeType, scope_value as scopeValue from user_data_scopes where user_id=#{userId}")
    List<Map<String, Object>> selectDataScopes(@Param("userId") Long userId);

    @Update("update users set real_name=#{realName} where username=#{username}")
    void updateUserRealName(@Param("username") String username, @Param("realName") String realName);

    @Select("select password from users where username=#{username}")
    String selectPasswordByUsername(@Param("username") String username);

    @Update("update users set password=#{password} where username=#{username}")
    void updateUserPassword(@Param("username") String username, @Param("password") String password);

    @Select("select count(*) from information_schema.columns where table_schema = database() and table_name = 'warehouses' and column_name = 'code'")
    Integer countWarehouseCodeColumn();

    @SelectProvider(type = SystemSqlProvider.class, method = "buildWarehouseListSql")
    List<Map<String, Object>> selectWarehouses(@Param("hasCode") boolean hasCode,
                                               @Param("keyword") String keyword,
                                               @Param("keywordLike") String keywordLike,
                                               @Param("status") String status);

    @Select("<script>select id, <if test='hasCode'>code, </if> name, address, status from warehouses where id=#{id}</script>")
    Map<String, Object> selectWarehouseDetail(@Param("id") Long id, @Param("hasCode") boolean hasCode);

    @Insert("insert into warehouses(code, name, address, status) values(#{code}, #{name}, #{address}, #{status})")
    void insertWarehouseWithCode(@Param("code") String code,
                                 @Param("name") String name,
                                 @Param("address") String address,
                                 @Param("status") String status);

    @Insert("insert into warehouses(name, address, status) values(#{name}, #{address}, #{status})")
    void insertWarehouseWithoutCode(@Param("name") String name,
                                    @Param("address") String address,
                                    @Param("status") String status);

    @Update("update warehouses set name=#{name}, address=#{address}, status=#{status} where id=#{id}")
    void updateWarehouse(@Param("id") Long id,
                         @Param("name") String name,
                         @Param("address") String address,
                         @Param("status") String status);

    @Update("update warehouses set status=#{status} where id=#{id}")
    void updateWarehouseStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("insert into roles(code, name, remark) values(#{code}, #{name}, #{remark})")
    void insertRole(@Param("code") String code, @Param("name") String name, @Param("remark") String remark);

    @Select("select name, remark from roles where code=#{code}")
    Map<String, Object> selectRoleByCode(@Param("code") String code);

    @Update("update roles set name=#{name}, remark=#{remark} where code=#{code}")
    void updateRole(@Param("code") String code, @Param("name") String name, @Param("remark") String remark);

    @Delete("delete from role_permissions where role_code=#{roleCode}")
    void deleteRolePermissions(@Param("roleCode") String roleCode);

    @Insert("insert into role_permissions(role_code, permission_code) values(#{roleCode}, #{permissionCode})")
    void insertRolePermission(@Param("roleCode") String roleCode, @Param("permissionCode") String permissionCode);

    @Select("select id, code, name, remark from roles order by id")
    List<Map<String, Object>> selectRoles();

    @Select("select permission_code from role_permissions where role_code=#{roleCode} order by permission_code")
    List<String> selectRolePermissions(@Param("roleCode") String roleCode);

    @Select("select id, code, name, module, remark from permissions order by id")
    List<Map<String, Object>> selectPermissions();

    @Insert("insert into permissions(code, name, module, remark) values(#{code}, #{name}, #{module}, #{remark})")
    void insertPermission(@Param("code") String code, @Param("name") String name, @Param("module") String module, @Param("remark") String remark);
}
