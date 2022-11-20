package com.atguigu.yygh.payment.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.payment.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(value = "点击微信支付调用的接口")
@RestController
@RequestMapping("/api/payment/weixin")
public class WeixinController {
	@Autowired
	WeixinService weixinService;

	//点击订单详情页的支付按钮
	//创建该订单的支付链接
	@ApiOperation(value = "")
	@GetMapping("/createNative/{orderId}")
	public R createNative(@PathVariable("orderId") Long orderId) {
		Map map = weixinService.createNative(orderId);
		return R.ok().data(map);//map中有支付链接
	}
}
