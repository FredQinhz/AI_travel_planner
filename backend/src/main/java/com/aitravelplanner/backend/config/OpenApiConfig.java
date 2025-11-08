package com.aitravelplanner.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI配置类
 * 用于配置API文档生成
 */
@Configuration
public class OpenApiConfig {

    /**
     * 创建OpenAPI Bean，配置API文档信息
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Travel Planner API文档")
                        .description("AI旅行规划器后端API接口文档")
                        .contact(new Contact()
                                .name("AI Travel Planner Team")
                                .email("")
                                .url(""))
                        .version("1.0"));
    }
}