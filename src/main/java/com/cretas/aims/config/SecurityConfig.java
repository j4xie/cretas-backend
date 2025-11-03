package com.cretas.aims.config;

import com.cretas.aims.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Spring Security配置
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-10
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（因为使用JWT）
                .csrf().disable()
                // 启用CORS（允许跨域请求）
                .cors().and()
                // 设置会话管理为无状态
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 配置URL访问权限
                .authorizeRequests()
                // 公开的认证接口（已实现的统一登录和设备激活）
                .antMatchers(
                        "/api/mobile/auth/**",        // 统一登录、刷新令牌、登出
                        "/api/mobile/activation/**"   // 设备激活
                ).permitAll()
                // AI成本分析接口（开发/测试环境公开，生产环境建议需要认证）
                .antMatchers(
                        "/api/mobile/*/processing/ai-service/health",         // AI服务健康检查
                        "/api/mobile/*/processing/batches/*/ai-cost-analysis", // AI成本分析
                        "/api/mobile/*/processing/ai-sessions/*"               // AI对话历史
                ).permitAll()
                // Swagger和API文档
                .antMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v2/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()
                // 健康检查端点
                .antMatchers("/actuator/**").permitAll()
                // 测试端点（仅开发环境）
                .antMatchers("/api/test/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
                .and()
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
}
}
