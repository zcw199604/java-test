package com.example.tobacco.auth;

import com.example.tobacco.util.PasswordCodec;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ShiroRealm extends AuthorizingRealm {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordCodec passwordCodec;

    public ShiroRealm(JdbcTemplate jdbcTemplate, PasswordCodec passwordCodec) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordCodec = passwordCodec;
        setCredentialsMatcher(new SimpleCredentialsMatcher());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        ShiroUserPrincipal principal = (ShiroUserPrincipal) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addRole(principal.getRoleCode());
        info.addStringPermissions(principal.getPermissions());
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken authToken = (UsernamePasswordToken) token;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.username, u.password, u.role_code, u.status from users u where u.username = ?",
                authToken.getUsername());
        if (rows.isEmpty()) {
            throw new UnknownAccountException("用户名或密码错误");
        }
        Map<String, Object> row = rows.get(0);
        if (!"ENABLED".equals(String.valueOf(row.get("status")))) {
            throw new DisabledAccountException("当前账号已禁用");
        }
        String encodedPassword = String.valueOf(row.get("password"));
        String rawPassword = authToken.getPassword() == null ? "" : new String(authToken.getPassword());
        if (!passwordCodec.matches(authToken.getUsername(), rawPassword, encodedPassword)) {
            throw new IncorrectCredentialsException("用户名或密码错误");
        }
        ShiroUserPrincipal principal = new ShiroUserPrincipal();
        principal.setUserId(((Number) row.get("id")).longValue());
        principal.setUsername(String.valueOf(row.get("username")));
        principal.setRoleCode(String.valueOf(row.get("role_code")));
        principal.setPermissions(loadPermissions(principal.getRoleCode()));
        return new SimpleAuthenticationInfo(principal, rawPassword, getName());
    }

    private Set<String> loadPermissions(String roleCode) {
        List<String> list = jdbcTemplate.queryForList(
                "select permission_code from role_permissions where role_code = ?",
                String.class,
                roleCode);
        return new LinkedHashSet<String>(list);
    }
}
