package com.atguigu.yygh.order;

import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class OrderTest {
	@Autowired
	OrderService orderService;
	@Autowired
	private OrderInfoMapper orderInfoMapper;

	@Test
	public void test1() {
		String sid = "6375dc656f7f450a4afcfea5";
		Long pid = 7L;
		orderService.saveOrder(sid, pid);
	}

	@Test
	public void test2() {
		List<OrderCountVo> orderCountVos = orderInfoMapper.selectOrderCount(new OrderCountQueryVo());
		orderCountVos.forEach(System.out::println);
	}
}
