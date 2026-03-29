package com.example.tobacco;

import org.springframework.boot.SpringApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.tobacco.mapper")
@SpringBootApplication
public class TobaccoPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TobaccoPlatformApplication.class, args);
    }
}
