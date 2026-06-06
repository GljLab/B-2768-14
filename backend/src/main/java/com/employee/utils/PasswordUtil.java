package com.employee.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 主方法 - 用于生成加密密码
     */
    public static void main(String[] args) {
        String password = "123456";
        String encoded = encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密密码: " + encoded);
        System.out.println("验证结果: " + matches(password, encoded));
    }
}
