package com.atguigu.yygh.payment.service;

import java.util.Map;

public interface WeixinService {
	/**
	 * @param orderId 平台端的订单id
	 * @return map（包括支付链接）
	 */
	Map createNative(Long orderId);
}
