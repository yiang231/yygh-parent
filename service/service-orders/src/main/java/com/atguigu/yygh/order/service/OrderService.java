package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<OrderInfo> {
	/**
	 * @param scheduleId 排班id
	 * @param patientId  就诊人id
	 * @return （平台端的订单id） ---> 下一个页面，订单详情，根据id查询订单（mg）
	 */
	Long saveOrder(String scheduleId, Long patientId);

	// 根据订单第查询订单详情（`yygh_order``order_info`）
	OrderInfo getOrderInfo(Long id);
}
