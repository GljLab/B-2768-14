package com.employee.config;

import com.employee.service.SessionService;
import com.employee.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return sendUnauthorized(response, "未授权,请先登录");
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            return sendUnauthorized(response, "Token无效或已过期");
        }
        
        if (!sessionService.isTokenValid(token)) {
            return sendUnauthorized(response, "会话已失效，请重新登录");
        }
        
        String role = jwtUtil.getRoleFromToken(token);
        String requestUri = request.getRequestURI();
        
        if (requestUri.startsWith("/api/upload")) {
        } else if (requestUri.startsWith("/api/employees") && !JwtUtil.ROLE_ADMIN.equals(role)) {
            return sendForbidden(response, "无权访问此页面");
        } else if (requestUri.startsWith("/api/employee/") && !JwtUtil.ROLE_EMPLOYEE.equals(role)) {
            return sendForbidden(response, "无权访问此页面");
        } else if (requestUri.startsWith("/api/admin/") && !JwtUtil.ROLE_ADMIN.equals(role)) {
            return sendForbidden(response, "无权访问此页面");
        } else if (requestUri.startsWith("/api/auth/employee/first-login/change-password") && !JwtUtil.ROLE_EMPLOYEE.equals(role)) {
            return sendForbidden(response, "无权访问此页面");
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        request.setAttribute("employeeId", jwtUtil.getEmployeeIdFromToken(token));
        request.setAttribute("adminId", jwtUtil.getAdminIdFromToken(token));
        
        return true;
    }
    
    private boolean sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
        return false;
    }
    
    private boolean sendForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + message + "\"}");
        return false;
    }
}
