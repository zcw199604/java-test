package com.example.tobacco;

import com.example.tobacco.mapper.auth.AuthSqlProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSqlProviderTest {

    private final AuthSqlProvider provider = new AuthSqlProvider();

    @Test
    void shouldBuildPrimaryCompatibilityColumnSql() {
        Map<String, Object> captchaParams = new HashMap<String, Object>();
        captchaParams.put("keyColumn", "captcha_key");
        captchaParams.put("codeColumn", "captcha_code");
        captchaParams.put("expireColumn", "expire_at");
        String captchaSql = provider.buildSelectCaptchaSql(captchaParams);
        assertThat(captchaSql).contains("captcha_key=#{captchaKey}");
        assertThat(captchaSql).contains("captcha_code as captchaCode");
        assertThat(captchaSql).contains("expire_at as expireAt");

        Map<String, Object> sessionParams = new HashMap<String, Object>();
        sessionParams.put("hasUsername", true);
        sessionParams.put("hasRoleCode", true);
        sessionParams.put("tokenColumn", "session_token");
        sessionParams.put("expireColumn", "expire_at");
        String sessionSql = provider.buildSelectSessionSql(sessionParams);
        assertThat(sessionSql).contains("us.username as sessionUsername");
        assertThat(sessionSql).contains("us.role_code as sessionRoleCode");
        assertThat(sessionSql).contains("us.expire_at as expireAt");
        assertThat(sessionSql).contains("us.session_token=#{token}");

        Map<String, Object> resetParams = new HashMap<String, Object>();
        resetParams.put("tokenColumn", "reset_token");
        resetParams.put("expireColumn", "expire_at");
        String resetSql = provider.buildSelectPasswordResetSql(resetParams);
        assertThat(resetSql).contains("pr.expire_at as expireAt");
        assertThat(resetSql).contains("pr.reset_token=#{resetToken}");
    }

    @Test
    void shouldBuildInsertSqlWithOptionalCompatibilityColumns() {
        Map<String, Object> sessionInsertParams = new HashMap<String, Object>();
        sessionInsertParams.put("hasUsername", true);
        sessionInsertParams.put("hasRoleCode", true);
        sessionInsertParams.put("tokenColumn", "session_token");
        sessionInsertParams.put("expireColumn", "expire_at");
        String sessionInsertSql = provider.buildInsertUserSessionSql(sessionInsertParams);
        assertThat(sessionInsertSql).contains("insert into user_sessions(session_token,user_id,username,role_code,expire_at,status)");
        assertThat(sessionInsertSql).contains("values(#{token},#{userId},#{username},#{roleCode},#{expireAt},'ACTIVE')");

        Map<String, Object> resetInsertParams = new HashMap<String, Object>();
        resetInsertParams.put("hasUsername", true);
        resetInsertParams.put("tokenColumn", "reset_token");
        resetInsertParams.put("expireColumn", "expire_at");
        String resetInsertSql = provider.buildInsertPasswordResetSql(resetInsertParams);
        assertThat(resetInsertSql).contains("insert into password_reset_records(user_id,username,reset_token,expire_at,status)");
        assertThat(resetInsertSql).contains("values(#{userId},#{username},#{resetToken},#{expireAt},'NEW')");
    }
}
