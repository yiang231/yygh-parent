package com.atguigu.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.atguigu"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//不使用数据库，取消数据源自动配置
public class ServiceMsmApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceMsmApplication.class, args);
	}
}
