package com.atguigu.yygh.payment.service;

import com.atguigu.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface WeixinService {
	/**
	 * @param orderId 平台端的订单id
	 * @return map（包括支付链接）
	 */
	Map createNative(Long orderId);

	Map<String, String> queryPayStatus(Long orderId);

	boolean isRefund(PaymentInfo paymentInfo);
}
