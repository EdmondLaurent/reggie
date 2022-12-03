package com.edmond.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@Slf4j
@SpringBootApplication
@ServletComponentScan   //  开启这个注解相当于加入过滤器
public class ReggieMain {
    public static void main(String[] args) {
        SpringApplication.run(ReggieMain.class, args);
        log.info("项目启动成功...");
    }
}
