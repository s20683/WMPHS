package com.s20683.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin").setViewName("forward:/admin/index.html");
        registry.addViewController("/picking").setViewName("forward:/picking/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:/app/admin-static/");
        registry.addResourceHandler("/admin/static/**")
                .addResourceLocations("file:/app/admin-static/static/");

        registry.addResourceHandler("/picking/**")
                .addResourceLocations("file:/app/picking-static/");
        registry.addResourceHandler("/picking/static/**")
                .addResourceLocations("file:/app/picking-static/static/");
    }
}