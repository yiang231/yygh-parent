package com.atguigu.yygh.msm.listener;

import com.atguigu.yygh.mq.service.MqConst;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MsmReceiver {
	//监听指定队列
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),//监听的队列
			exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),//第一个队列使用的交换器
			key = {MqConst.ROUTING_MSM_ITEM}//队列和交换机绑定时指定的key
	))
	public void receiver(MsmVo msmVo) throws IOException {
		System.out.println("模拟给就诊人发送短息通知");
		String phone = msmVo.getPhone();
		String templateCode = msmVo.getTemplateCode();
		Object message = msmVo.getParam().get("message");

		System.out.println("手机号：" + phone + "|" + "短信内容：" + message);
	}
}
