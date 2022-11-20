package com.atguigu.yygh.payment.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.payment.service.PaymentInfoService;
import com.atguigu.yygh.payment.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(value = "点击微信支付调用的接口")
@RestController
@RequestMapping("/api/payment/weixin")
public class WeixinController {
	@Autowired
	private WeixinService weixinService;
	@Autowired
	private PaymentInfoService paymentInfoService;

	//点击订单详情页的支付按钮
	//创建该订单的支付链接
	@ApiOperation(value = "创建支付链接，生成支付二维码以及支付订单")
	@GetMapping("/createNative/{orderId}")
	public R createNative(@PathVariable("orderId") Long orderId) {
		Map map = weixinService.createNative(orderId);
		return R.ok().data(map);//map中有支付链接
	}

	@ApiOperation(value = "查询订单支付状态")
	@GetMapping("/queryPayStatus/{orderId}")
	public R queryPayStatus(@PathVariable("orderId") Long orderId) {
		Map<String, String> map = weixinService.queryPayStatus(orderId);

		if (map == null) {
			return R.error().message("支付出错");
		}

		String trade_state = map.get("trade_state");
		if (StringUtils.isEmpty(trade_state)) {
			return R.error().message("支付出错");
		}
		if (trade_state.equalsIgnoreCase("SUCCESS")) {
			//订单状态 + 支付记录状态
			paymentInfoService.afterPaySuccess(orderId, map);
			return R.ok().message("支付成功");
		}
		return R.ok().message("支付中");
	}

	@ApiOperation(value = "查询订单支付记录，取消订单时使用")
	@GetMapping("/getPaymentInfo/{orderId}/{payType}")
	public PaymentInfo getPaymentInfo(@PathVariable Long orderId, @PathVariable Integer payType) {
		return paymentInfoService.getPaymentInfo(orderId, payType);
	}

	@ApiOperation(value = "根据支付记录判断是否已经完成退款，取消订单时使用")
	@PostMapping("/isRefund")
	public boolean isRefund(@RequestBody PaymentInfo paymentInfo) {
		return weixinService.isRefund(paymentInfo);
	}
}
