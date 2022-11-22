package com.atguigu.yygh.task.task;

import com.atguigu.yygh.mq.service.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling //开启定时任务
@Component
public class ScheduledTask {
	@Autowired
	RabbitService rabbitService;

	//第一个定时任务   秒  分  时  日  月  星期  ，  星期：？ 跟星期几无关，   * 任意
//    @Scheduled(cron = "0 0 8 * * ?")//cron表达式的作用，用来定义时间规则  每天的8点整
	@Scheduled(cron = "0/10 * * * * ?")//每十秒  //(cron = "8 * * * * ?") 每分钟内的第八秒
	public void task1() {
		//System.out.println(new Date());
		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, new OrderMqVo());
	}
}
