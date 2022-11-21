package com.atguigu.yygh.order.listener;

import com.atguigu.yygh.mq.service.MqConst;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderReceiver {
    @Autowired
    OrderService orderService;

    //监听第三个队列
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8, durable = "true"),//监听的队列
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),//第一个队列使用的交换器
            key = {MqConst.ROUTING_TASK_8}//队列和交换机绑定时指定的key
    ))
    public void receiver(OrderMqVo orderMqVo) throws IOException {
        //查询今天的订单,遍历
        orderService.patientTips();
    }
}
