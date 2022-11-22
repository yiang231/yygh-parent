package com.atguigu.yygh.order.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "sentinel调用测试")
@RestController
@RequestMapping("/sentinel")
public class SentinelTestController {
	@GetMapping
	public String sentinel() {
		return "Hello Sentinel";
	}
}
