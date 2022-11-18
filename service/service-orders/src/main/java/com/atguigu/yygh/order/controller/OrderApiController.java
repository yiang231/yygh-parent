package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "预约挂号接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {
	@Autowired
	private OrderService orderService;

	// 平台端排班id（mg）
	@ApiOperation(value = "添加挂号订单")
	@PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
	public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
		Long orderId = orderService.saveOrder(scheduleId, patientId);
		return R.ok().data("orderId", orderId); // 返回给订单详情页面查询订单详情使用
	}

	// 订单详情页面调用
	@ApiOperation(value = "根据订单第查询订单详情")
	@GetMapping("auth/getOrder/{orderId}")
	public R getOrder(@PathVariable Long orderId) {
		OrderInfo orderInfo = orderService.getOrderInfo(orderId);
		return R.ok().data("orderInfo", orderInfo);
	}
}
