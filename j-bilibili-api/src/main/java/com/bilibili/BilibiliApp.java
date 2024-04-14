package com.bilibili;

import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app =  SpringApplication.run(BilibiliApp.class, args);
    }
}
