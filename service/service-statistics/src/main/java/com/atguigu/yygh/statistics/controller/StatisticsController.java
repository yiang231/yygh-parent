package com.atguigu.yygh.statistics.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.orders.client.OrdersFeignClient;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {
	@Autowired
	OrdersFeignClient ordersFeignClient;

	@GetMapping("getCountMap")
	public R getCountMap(OrderCountQueryVo orderCountQueryVo) {
		Map<String, Object> countMap = ordersFeignClient.getCountMap(orderCountQueryVo);
		return R.ok().data(countMap);
	}
}
