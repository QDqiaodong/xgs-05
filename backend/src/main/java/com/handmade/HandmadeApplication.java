package com.handmade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.handmade.mapper")
public class HandmadeApplication {
    public static void main(String[] args) {
        SpringApplication.run(HandmadeApplication.class, args);
    }
}
