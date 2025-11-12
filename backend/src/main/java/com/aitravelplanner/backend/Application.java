package com.aitravelplanner.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * AI Travel Planner Backend Application
 * 这是Spring Boot应用的入口类
 */
@SpringBootApplication
@EnableAsync
public class Application {

    public static void main(String[] args) {
        // 加载 .env 文件到系统属性
        // 这样 Spring Boot 就可以通过 ${VAR_NAME} 读取环境变量了
        loadEnvFile();
        
        SpringApplication.run(Application.class, args);
    }

    /**
     * 从 .env 文件加载环境变量到系统属性
     */
    private static void loadEnvFile() {
        try {
            // 尝试从当前工作目录和项目根目录查找 .env 文件
            File envFile = new File(".env");
            if (!envFile.exists()) {
                // 如果在当前目录找不到，尝试在项目根目录查找
                String projectRoot = System.getProperty("user.dir");
                envFile = Paths.get(projectRoot, ".env").toFile();
            }
            
            if (!envFile.exists()) {
                System.out.println("Info: .env file not found, using system environment variables.");
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 跳过空行和注释
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    // 解析 KEY=VALUE 格式
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        
                        // 移除引号（如果有）
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        // 只有当系统属性中不存在该变量时才设置（优先使用系统环境变量）
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
                System.out.println("Info: Successfully loaded .env file.");
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load .env file: " + e.getMessage() + 
                             ", using system environment variables instead.");
        } catch (Exception e) {
            System.out.println("Warning: Error loading .env file: " + e.getMessage() + 
                             ", using system environment variables instead.");
        }
    }

}