package com.atguigu.yygh.mq.service;

public class MqConst {
	/**
	 * 预约下单   第一个队列
	 */
	public static final String EXCHANGE_DIRECT_ORDER = "exchange.direct.order";
	public static final String ROUTING_ORDER = "order";
	public static final String QUEUE_ORDER = "queue.order";//第一个队列的名字

	/**
	 * 短信   第二个队列
	 */
	public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm";
	public static final String ROUTING_MSM_ITEM = "msm.item";
	public static final String QUEUE_MSM_ITEM = "queue.msm.item";

	//定时任务  第三个队列
	public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
	public static final String ROUTING_TASK_8 = "task.8";
	//队列
	public static final String QUEUE_TASK_8 = "queue.task.8";
}
