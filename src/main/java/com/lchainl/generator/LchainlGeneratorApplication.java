package com.lchainl.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 *
 * @author ethan
 *
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.lchainl.**.dao"})
@ComponentScan(basePackages = "com.lchainl.generator")
public class LchainlGeneratorApplication extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(LchainlGeneratorApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(LchainlGeneratorApplication.class, args);
    }
}
