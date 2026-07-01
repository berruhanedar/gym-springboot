package com.berruhanedar.app.gym_springboot.config;

import com.berruhanedar.app.gym_springboot.logging.RestCallLoggingInterceptor;
import com.berruhanedar.app.gym_springboot.logging.TransactionIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TransactionIdInterceptor());
        registry.addInterceptor(new RestCallLoggingInterceptor());
    }
}
