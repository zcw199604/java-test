package com.example.tobacco.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.stereotype.Component;

@Component
public class PasswordCodec {
    private static final String ALGORITHM = "SHA-256";
    private static final int ITERATIONS = 1024;

    public String encode(String username, String password) {
        return new SimpleHash(ALGORITHM, password, ByteSource.Util.bytes(username), ITERATIONS).toHex();
    }

    public boolean matches(String username, String rawPassword, String encodedPassword) {
        return rawPassword != null && (rawPassword.equals(encodedPassword) || encode(username, rawPassword).equals(encodedPassword));
    }
}
