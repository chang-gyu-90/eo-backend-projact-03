package com.example.mlbf;

import com.example.mlbf.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MlbfApplication {

    public static void main(String[] args) {
        SpringApplication.run(MlbfApplication.class, args);
    }
}
