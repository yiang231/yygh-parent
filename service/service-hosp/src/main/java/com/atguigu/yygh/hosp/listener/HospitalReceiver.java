package com.atguigu.yygh.hosp.listener;

import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.mq.service.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HospitalReceiver {
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleRepository scheduleRepository;
	@Autowired
	RabbitService rabbitService;

	//监听指定队列
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),//监听的队列
			exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),//第一个队列使用的交换器
			key = {MqConst.ROUTING_ORDER}//队列和交换机绑定时指定的key
	))
	public void receiver(OrderMqVo orderMqVo) throws IOException {
		String scheduleId = orderMqVo.getScheduleId();
		Integer availableNumber = orderMqVo.getAvailableNumber();
		Integer reservedNumber = orderMqVo.getReservedNumber();

		scheduleService.updateSchedule(scheduleId, availableNumber, reservedNumber);

		//向第二个队列发消息
		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, orderMqVo.getMsmVo());
	}
}
