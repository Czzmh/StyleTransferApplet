package com.demo;

import com.demo.utils.SpeechClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/23 14:20
 */
@SpringBootApplication
@MapperScan("com.demo.database.mapper")
public class StartUpApplication {

    public static void main(String[] args){
        SpringApplication.run(StartUpApplication.class, args);
    }
}
