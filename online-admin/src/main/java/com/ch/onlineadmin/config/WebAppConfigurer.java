package com.wzz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebAppConfigurer implements WebMvcConfigurer {

    @Bean
    public com.wzz.config.AdminInterceptor getAdminInterceptor() {//只属于管理员
        return new com.wzz.config.AdminInterceptor();
    }

    @Bean
    public com.wzz.config.TeacherInterceptor getTeacherInterceptor() {//属于老师 但是管理员也可以用
        return new com.wzz.config.TeacherInterceptor();
    }

    @Bean
    public com.wzz.config.StudentInterceptor getStudentInterceptor() {//属于学生 但是管理员也可以用
        return new com.wzz.config.StudentInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 可添加多个
        //拦截未登录进入超级管理员的界面
        registry.addInterceptor(getAdminInterceptor()).addPathPatterns("/admin/**");
        registry.addInterceptor(getTeacherInterceptor()).addPathPatterns("/teacher/**");
        registry.addInterceptor(getStudentInterceptor()).addPathPatterns("/student/**");
    }

}