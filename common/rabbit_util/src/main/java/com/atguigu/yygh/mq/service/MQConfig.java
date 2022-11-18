package com.atguigu.yygh.mq.service;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
	//发送消息或者接收消息时使用自定的消息对象，就需要配置消息转换器
	//消息转换器
	// 自定义消息对象（XxxVo） ----> 自定义的消息对象转成byte字节---》Message对象 ===消息发送===》
	// 《===Message对象-->byte字节--->转成自定义消息对象
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
