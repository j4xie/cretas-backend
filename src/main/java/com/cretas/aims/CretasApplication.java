package com.cretas.aims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
/**
 * 白垩纪食品溯源系统 - Spring Boot启动类
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EntityScan("com.cretas.aims.entity")
@EnableJpaRepositories("com.cretas.aims.repository")
public class CretasApplication {
    public static void main(String[] args) {
        SpringApplication.run(CretasApplication.class, args);
        System.out.println("========================================");
        System.out.println("     白垩纪食品溯源系统启动成功！");
        System.out.println("     Cretas Backend System Started!");
    }
}
