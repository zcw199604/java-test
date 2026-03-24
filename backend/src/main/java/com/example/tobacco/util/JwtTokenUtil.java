package com.example.tobacco.util;

import org.springframework.stereotype.Component;

/**
 * 兼容层：当前版本已迁移为 Shiro + user_sessions，会话不再依赖 JWT。
 * 保留该类仅避免历史引用导致编译失败；如仍被调用则明确抛错。
 */
@Component
public class JwtTokenUtil {

    public String generateToken(Long userId, String username, String roleCode) {
        throw new UnsupportedOperationException("当前版本已切换为 Shiro 会话，不再生成 JWT");
    }

    public Object parseToken(String token) {
        throw new UnsupportedOperationException("当前版本已切换为 Shiro 会话，不再解析 JWT");
    }
}
