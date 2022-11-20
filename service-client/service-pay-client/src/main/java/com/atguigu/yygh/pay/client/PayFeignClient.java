package com.atguigu.yygh.pay.client;

import com.atguigu.yygh.model.order.PaymentInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-pay")
public interface PayFeignClient {
	@GetMapping("/api/payment/weixin/getPaymentInfo/{orderId}/{payType}")//支付记录
	public PaymentInfo getPaymentInfo(@PathVariable Long orderId, @PathVariable Integer payType);

	@PostMapping("/api/payment/weixin/isRefund")//是否退款
	public boolean isRefund(@RequestBody PaymentInfo paymentInfo);
}
