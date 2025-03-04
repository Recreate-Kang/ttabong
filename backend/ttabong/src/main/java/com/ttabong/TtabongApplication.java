package com.ttabong;

import com.ttabong.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TtabongApplication {
    public static void main(String[] args) {

        SpringApplication.run(TtabongApplication.class, args);
    }
}
