package com.atguigu.yygh.payment.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface PaymentInfoService extends IService<PaymentInfo> {
	/**
	 * 为订单创建支付记录，支付记录中的属性值可以从orderInfo中获取
	 *
	 * @param orderInfo   订单
	 * @param paymentType 支付方法（2=微信支付）
	 */
	void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

	void afterPaySuccess(Long orderId, Map<String, String> map);
}
