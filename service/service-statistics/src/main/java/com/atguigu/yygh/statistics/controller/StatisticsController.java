package com.atguigu.yygh.statistics.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.orders.client.OrdersFeignClient;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(value = "数据统计")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {
	@Autowired
	OrdersFeignClient ordersFeignClient;

	@ApiOperation(value = "数据统计页面展示")
	@GetMapping("getCountMap")
	public R getCountMap(OrderCountQueryVo orderCountQueryVo) {
		Map<String, Object> countMap = ordersFeignClient.getCountMap(orderCountQueryVo);
		return R.ok().data(countMap);
	}
}
