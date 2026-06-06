package com.employee.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_EMPLOYEE = "employee";
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * 生成管理员JWT token
     */
    public String generateAdminToken(Long adminId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", ROLE_ADMIN);
        claims.put("adminId", adminId);
        return generateToken(username, claims);
    }
    
    /**
     * 生成员工JWT token
     */
    public String generateEmployeeToken(Long employeeId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", ROLE_EMPLOYEE);
        claims.put("employeeId", employeeId);
        return generateToken(email, claims);
    }
    
    /**
     * 生成JWT token
     */
    private String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 从token中获取用户名/邮箱
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * 从token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 从token中获取员工ID
     */
    public Long getEmployeeIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object employeeId = claims.get("employeeId");
        if (employeeId == null) {
            return null;
        }
        return Long.valueOf(employeeId.toString());
    }
    
    /**
     * 从token中获取管理员ID
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object adminId = claims.get("adminId");
        if (adminId == null) {
            return null;
        }
        return Long.valueOf(adminId.toString());
    }
    
    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
