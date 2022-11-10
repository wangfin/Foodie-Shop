package com.imooc;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// 打包 war 【4】添加war的启动类
public class WarStarterApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // 指向Application这个SpringBoot启动类
        return builder.sources(Application.class);
    }
}
