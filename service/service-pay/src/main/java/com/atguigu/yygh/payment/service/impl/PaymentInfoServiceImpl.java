package com.atguigu.yygh.payment.service.impl;

import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.orders.client.OrdersFeignClient;
import com.atguigu.yygh.payment.mapper.PaymentInfoMapper;
import com.atguigu.yygh.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
	@Autowired
	OrdersFeignClient ordersFeignClient;

	// 创建支付记录
	@Override
	public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {

		//每一个订单最多只有一条支付记录
		//根据订单id和支付方法查询支付记录（根据out_trade_no也可以）
		QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("order_id", orderInfo.getId());
		queryWrapper.eq("payment_type", paymentType);

		PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);

		if (paymentInfo != null) {
			return;
		}

		paymentInfo = new PaymentInfo();
		paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());//订单的out_trade_no和他的支付记录的out_trade_no是一致的
		paymentInfo.setOrderId(orderInfo.getId());
		paymentInfo.setPaymentType(paymentType);
//        paymentInfo.setTradeNo(); 支付成功后，微信端返回的流水号 ；  需要等到支付成功后再来赋值
		paymentInfo.setTotalAmount(orderInfo.getAmount());//订单金额

		String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|" + orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
		paymentInfo.setSubject(subject);
		paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());//1-支付中（未支付）
//        paymentInfo.setCallbackTime();  回调时间（支付完成后的时间）
//        paymentInfo.setCallbackContent(); 支付完成后，微信端返回的数据
		paymentInfo.setCreateTime(new Date());
		paymentInfo.setUpdateTime(new Date());

		baseMapper.insert(paymentInfo);
	}

	// 支付成功后修改订单，以及对应的支付记录
	@Override
	public void afterPaySuccess(Long orderId, Map<String, String> map) {
		OrderInfo orderInfo = ordersFeignClient.getOrderInfoById(orderId);
		orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());//1 -- 已支付
		orderInfo.setUpdateTime(new Date());
		ordersFeignClient.updateById(orderInfo);

		//查询订单的支付记录
		QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("out_trade_no", orderInfo.getOutTradeNo());
		queryWrapper.eq("payment_type", PaymentTypeEnum.WEIXIN.getStatus());

		PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
		paymentInfo.setUpdateTime(new Date());
		paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());//2---已支付
		String transaction_id = map.get("transaction_id");
		paymentInfo.setTradeNo(transaction_id);//微信端支付流水号
		paymentInfo.setCallbackTime(new Date());
		paymentInfo.setCallbackContent(map.toString());

		baseMapper.updateById(paymentInfo);

		// 此时医院端的订单状态没有修改
	}

	@Override
	public PaymentInfo getPaymentInfo(Long orderId, Integer payType) {
		QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("order_id", orderId);
		queryWrapper.eq("payment_type", payType);
		return baseMapper.selectOne(queryWrapper);
	}
}
