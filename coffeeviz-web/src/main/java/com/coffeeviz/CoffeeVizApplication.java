package com.coffeeviz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CoffeeViz 启动类
 */
@SpringBootApplication
@MapperScan("com.coffeeviz.mapper")
@ComponentScan(basePackages = {"com.coffeeviz"})
@EnableScheduling
public class CoffeeVizApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CoffeeVizApplication.class, args);
        System.out.println("====================================");
        System.out.println("CoffeeViz 启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("====================================");
    }
}
