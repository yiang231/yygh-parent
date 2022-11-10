package com.atguigu.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.atguigu"})//扫描到common模块包下的swagger2配置类
//@MapperScan(basePackages = {"com.atguigu.yygh.hosp.mapper"})
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu")//扫描基准包，不写则是当前启动类所在的包
public class ServiceHospApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceHospApplication.class, args);
	}
}
