package com.atguigu.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.atguigu.yygh.hosp.mapper")
@SpringBootApplication
public class ServiceHospApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceHospApplication.class, args);
	}
}