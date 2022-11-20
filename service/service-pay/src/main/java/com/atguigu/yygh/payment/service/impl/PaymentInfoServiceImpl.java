package com.atguigu.yygh.payment.service.impl;

import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.payment.mapper.PaymentInfoMapper;
import com.atguigu.yygh.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
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
}
