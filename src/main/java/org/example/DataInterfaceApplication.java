package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.config.Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("org.example.dao")
@SpringBootApplication
@EnableScheduling
@ComponentScan("org.example.*")
@Slf4j
public class DataInterfaceApplication {


    public static void main(String[] args) throws Exception {
        try {
            log.info("APP-VERSION: 2024-01-22.003");
            Config.init();
            //        运行
            SpringApplication.run(DataInterfaceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
