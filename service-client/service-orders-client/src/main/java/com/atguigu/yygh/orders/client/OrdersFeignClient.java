package com.atguigu.yygh.orders.client;

import com.atguigu.yygh.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-orders")
public interface OrdersFeignClient {
	@GetMapping("/api/order/orderInfo/getOrderInfoById/{orderId}")
	public OrderInfo getOrderInfoById(@PathVariable Long orderId);
}
