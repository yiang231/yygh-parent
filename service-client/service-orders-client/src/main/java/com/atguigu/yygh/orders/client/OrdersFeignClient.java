package com.atguigu.yygh.orders.client;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "service-orders")
public interface OrdersFeignClient {
	@GetMapping("/api/order/orderInfo/getOrderInfoById/{orderId}")
	public OrderInfo getOrderInfoById(@PathVariable Long orderId);

	@PutMapping("/api/order/orderInfo/updateById")
	public void updateById(@RequestBody OrderInfo orderInfo);

	@PostMapping("/api/order/orderInfo/inner/getCountMap")   //页面上的查询条件  医院名称 + 日期范围
	public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
