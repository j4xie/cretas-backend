package com.cretas.aims.security;

import com.cretas.aims.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
/**
 * JWT认证过滤器
 * 从请求头中提取JWT token并验证
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                Integer userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                if (userId != null) {
                    // 创建认证对象，从token中提取角色信息
                    java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

                    // 如果token中有role，使用token中的role
                    if (role != null && !role.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority(role));
                        log.debug("从token中提取角色: {}", role);
                    } else {
                        // 兼容旧token，默认给ROLE_USER权限
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        log.debug("token中无角色信息，使用默认角色: ROLE_USER");
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 设置到SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, role);
                }
            }
        } catch (Exception e) {
            log.error("认证失败: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
    /**
     * 从请求头中提取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
