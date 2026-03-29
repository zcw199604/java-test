package com.example.tobacco.auth;

import com.example.tobacco.mapper.auth.AuthMapper;
import com.example.tobacco.util.PasswordCodec;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ShiroRealm extends AuthorizingRealm {

    private final AuthMapper authMapper;
    private final PasswordCodec passwordCodec;

    public ShiroRealm(AuthMapper authMapper, PasswordCodec passwordCodec) {
        this.authMapper = authMapper;
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
        Map<String, Object> row = authMapper.selectRealmUserByUsername(authToken.getUsername());
        if (row == null || row.isEmpty()) {
            throw new UnknownAccountException("用户名或密码错误");
        }
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
        principal.setRoleCode(String.valueOf(row.get("roleCode")));
        principal.setPermissions(loadPermissions(principal.getRoleCode()));
        return new SimpleAuthenticationInfo(principal, rawPassword, getName());
    }

    private Set<String> loadPermissions(String roleCode) {
        List<String> list = authMapper.selectPermissionsByRoleCode(roleCode);
        return new LinkedHashSet<String>(list);
    }
}
