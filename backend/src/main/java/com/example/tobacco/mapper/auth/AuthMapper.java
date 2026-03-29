package com.example.tobacco.mapper.auth;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AuthMapper {

    @Select("select count(*) from information_schema.columns where table_schema = database() and table_name = #{tableName} and column_name = #{columnName}")
    Integer countTableColumn(@Param("tableName") String tableName, @Param("columnName") String columnName);

    @Select("select u.id, u.username, u.password, u.real_name as realName, u.role_code as roleCode, u.status, r.name as roleName from users u left join roles r on u.role_code = r.code where u.username = #{username} limit 1")
    Map<String, Object> selectLoginUserByUsername(@Param("username") String username);

    @Select("select u.id, u.username, u.real_name as realName, u.role_code as roleCode, u.status, r.name as roleName from users u left join roles r on u.role_code = r.code where u.username = #{username} limit 1")
    Map<String, Object> selectProfileByUsername(@Param("username") String username);

    @Select("select u.id, u.username, u.password, u.role_code as roleCode, u.status from users u where u.username = #{username} limit 1")
    Map<String, Object> selectRealmUserByUsername(@Param("username") String username);

    @Select("select permission_code from role_permissions where role_code = #{roleCode} order by permission_code")
    List<String> selectPermissionsByRoleCode(@Param("roleCode") String roleCode);

    @Select("select username from users where id = #{userId}")
    String selectUsernameByUserId(@Param("userId") Long userId);

    @Select("select id from users where username = #{username}")
    Long selectUserIdByUsername(@Param("username") String username);

    @Update("update users set password = #{password} where username = #{username}")
    void updateUserPasswordByUsername(@Param("username") String username, @Param("password") String password);

    @SelectProvider(type = AuthSqlProvider.class, method = "buildSelectCaptchaSql")
    Map<String, Object> selectLatestCaptchaRecord(@Param("keyColumn") String keyColumn,
                                                  @Param("codeColumn") String codeColumn,
                                                  @Param("expireColumn") String expireColumn,
                                                  @Param("captchaKey") String captchaKey);

    @Update("update captcha_records set status='USED' where id = #{id}")
    void markCaptchaUsed(@Param("id") Long id);

    @InsertProvider(type = AuthSqlProvider.class, method = "buildInsertCaptchaSql")
    void insertCaptchaRecord(@Param("keyColumn") String keyColumn,
                             @Param("codeColumn") String codeColumn,
                             @Param("expireColumn") String expireColumn,
                             @Param("captchaKey") String captchaKey,
                             @Param("captchaCode") String captchaCode,
                             @Param("expireAt") LocalDateTime expireAt);

    @SelectProvider(type = AuthSqlProvider.class, method = "buildSelectSessionSql")
    Map<String, Object> selectLatestSessionByToken(@Param("hasUsername") boolean hasUsername,
                                                   @Param("hasRoleCode") boolean hasRoleCode,
                                                   @Param("tokenColumn") String tokenColumn,
                                                   @Param("expireColumn") String expireColumn,
                                                   @Param("token") String token);

    @UpdateProvider(type = AuthSqlProvider.class, method = "buildUpdateSessionStatusSql")
    void updateSessionStatus(@Param("tokenColumn") String tokenColumn,
                             @Param("status") String status,
                             @Param("token") String token);

    @UpdateProvider(type = AuthSqlProvider.class, method = "buildTouchSessionSql")
    void touchSessionLastAccess(@Param("tokenColumn") String tokenColumn,
                                @Param("token") String token);

    @InsertProvider(type = AuthSqlProvider.class, method = "buildInsertUserSessionSql")
    void insertUserSession(@Param("hasUsername") boolean hasUsername,
                           @Param("hasRoleCode") boolean hasRoleCode,
                           @Param("tokenColumn") String tokenColumn,
                           @Param("expireColumn") String expireColumn,
                           @Param("token") String token,
                           @Param("userId") Long userId,
                           @Param("username") String username,
                           @Param("roleCode") String roleCode,
                           @Param("expireAt") LocalDateTime expireAt);

    @SelectProvider(type = AuthSqlProvider.class, method = "buildSelectPasswordResetSql")
    Map<String, Object> selectLatestPasswordReset(@Param("tokenColumn") String tokenColumn,
                                                  @Param("expireColumn") String expireColumn,
                                                  @Param("username") String username,
                                                  @Param("resetToken") String resetToken);

    @UpdateProvider(type = AuthSqlProvider.class, method = "buildUpdatePasswordResetUsedSql")
    void updatePasswordResetUsed(@Param("hasUsedAt") boolean hasUsedAt,
                                 @Param("id") Long id);

    @InsertProvider(type = AuthSqlProvider.class, method = "buildInsertPasswordResetSql")
    void insertPasswordResetRecord(@Param("hasUsername") boolean hasUsername,
                                   @Param("tokenColumn") String tokenColumn,
                                   @Param("expireColumn") String expireColumn,
                                   @Param("userId") Long userId,
                                   @Param("username") String username,
                                   @Param("resetToken") String resetToken,
                                   @Param("expireAt") LocalDateTime expireAt);
}
