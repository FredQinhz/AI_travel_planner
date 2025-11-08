package com.aitravelplanner.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.aitravelplanner.backend.repository")
@EnableTransactionManagement
public class JpaConfig {
    // 配置已经通过application.yml完成
    // 此配置类确保Repository接口被正确扫描和启用
}