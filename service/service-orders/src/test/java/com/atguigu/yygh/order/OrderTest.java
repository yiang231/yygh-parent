package com.atguigu.yygh.order;

import com.atguigu.yygh.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderTest {
	@Autowired
	OrderService orderService;

	@Test
	public void test1() {
		String sid = "6375dc656f7f450a4afcfea5";
		Long pid = 7L;
		orderService.saveOrder(sid, pid);
	}
}
