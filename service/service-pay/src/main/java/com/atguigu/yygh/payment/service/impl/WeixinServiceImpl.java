package com.atguigu.yygh.payment.service.impl;

import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.orders.client.OrdersFeignClient;
import com.atguigu.yygh.payment.service.PaymentInfoService;
import com.atguigu.yygh.payment.service.RefundInfoService;
import com.atguigu.yygh.payment.service.WeixinService;
import com.atguigu.yygh.payment.util.ConstantPropertiesUtils;
import com.atguigu.yygh.payment.util.HttpClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinServiceImpl implements WeixinService {
	@Autowired
	RedisTemplate<String, Object> redisTemplate; // RedisTemplate<String,Object>
	@Autowired
	PaymentInfoService paymentInfoService;
	@Autowired
	OrdersFeignClient ordersFeignClient;
	@Autowired
	private RefundInfoService refundInfoService;

	@Override
	public Map createNative(Long orderId) {

		//1、判断redis中是否存在支付链接（map）
//		stringRedisTemplate.boundValueOps(orderId.toString()).get();// 存进去的是带有支付链接的map，得用Object进行转换，这里不要用StringRedisTemplate
		Map map = (Map) redisTemplate.boundValueOps(orderId.toString()).get();
		if (map != null) {
			return map;
		}

		//2、为订单创建支付记录 `yygh_order`.`payment_info` （注意：每一个订单最多只能有一个支付记录）
		//支付记录的支付状态 = 未支付
		OrderInfo orderInfo = ordersFeignClient.getOrderInfoById(orderId); // 远程调用
		paymentInfoService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());

		//3、封装参数
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("appid", ConstantPropertiesUtils.APPID);
		paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
		paramMap.put("nonce_str", WXPayUtil.generateNonceStr());// 生成随机字符串

		String reserveDateString = new DateTime(orderInfo.getReserveDate()).toString("yyyy/MM/dd");
		String body = reserveDateString + "就诊" + orderInfo.getDepname();
		paramMap.put("body", body);
		paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
		BigDecimal amount = orderInfo.getAmount();//元
//        long total_fee = amount.multiply(new BigDecimal(100)).longValue(); // 真实挂号费金额
		long total_fee = 1;
		paramMap.put("total_fee", total_fee + "");//测试支付1分
		paramMap.put("spbill_create_ip", "127.0.0.1"); // 终端ip
		paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");//微信支付成功或失败之后，自动回到的接口，并且会将支付状态传递给该接口  没用到必须传
		paramMap.put("trade_type", "NATIVE");
//        paramMap.put("sign",ConstantPropertiesUtils.PARTNERKEY);//签名

		try {
			//4、map转成xml格式的字符串，带有sign签名key的
			String xmlString = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);

			//5、调用微信端“统一下单”接口
			String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			HttpClient httpClient = new HttpClient(url);
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlString);//xml格式的字符串参数
			httpClient.post();

			//6、解析微信端的返回值
			String content = httpClient.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

			//7、封装返回结果
			Map<String, Object> result = new HashMap<>();
			result.put("codeUrl", resultMap.get("code_url"));//放在二维码中
			result.put("orderId", orderId);
			result.put("totalFee", orderInfo.getAmount());
			result.put("resultCode", resultMap.get("result_code"));//SUCCESS/FAIL

			//8、返回结果存入到redis中
			redisTemplate.boundValueOps(orderId.toString()).set(result, 5, TimeUnit.MINUTES);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;//支付链接在map中
	}

	@Override
	public Map<String, String> queryPayStatus(Long orderId) {
		//1、根据订单id查询订单，为了获取到outTradeNo
		OrderInfo orderInfo = ordersFeignClient.getOrderInfoById(orderId); // 远程调用

		// 2、封装参数
		Map<String, String> map = new HashMap<>();
		map.put("appid", ConstantPropertiesUtils.APPID);
		map.put("mch_id", ConstantPropertiesUtils.PARTNER);
		map.put("out_trade_no", orderInfo.getOutTradeNo());
		map.put("nonce_str", WXPayUtil.generateNonceStr());

		try {
			//3、map转成xml格式的字符串，并且自动添加sign签名
			String xmlString = WXPayUtil.generateSignedXml(map, ConstantPropertiesUtils.PARTNERKEY);

			//4、发请求（查询订单状态）
			String url = "https://api.mch.weixin.qq.com/pay/orderquery";
			HttpClient httpClient = new HttpClient(url);
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlString);
			httpClient.post();

			//5、微信端返回值
			String content = httpClient.getContent();// 返回值
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isRefund(PaymentInfo paymentInfo) {
		//1、查询该订单对应的退款记录（最多一条）
		QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
		String outTradeNo = paymentInfo.getOutTradeNo();
		queryWrapper.eq("out_trade_no", outTradeNo);//order_id 或 outTradeNo
		queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
		RefundInfo refundInfo = refundInfoService.getOne(queryWrapper);

		//2、如果退款记录不存在，创建退款记录 UNREFUND(1,"退款中"),  REFUND(2,"已退款");
		if (refundInfo == null) {
			refundInfo = new RefundInfo();
			//从支付记录中取出值赋值给退款记录
			refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());//订单 --》 支付记录 --- > 退款记录
			refundInfo.setOrderId(paymentInfo.getOrderId());
			refundInfo.setPaymentType(paymentInfo.getPaymentType());
			refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
			refundInfo.setSubject(paymentInfo.getSubject());
			refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());//1--退款中
//			refundInfo.setTradeNo();// 退款成功后的流水号
//            refundInfo.setCallbackTime();// 退款成功后赋值
//            refundInfo.setCallbackContent();
			refundInfo.setCreateTime(new Date());
			refundInfo.setUpdateTime(new Date());

			refundInfoService.save(refundInfo);
		}

		//3、根据退款记录中的退款状态判断是否已经完成退款
		if (refundInfo.getRefundStatus() == RefundStatusEnum.REFUND.getStatus()) {
			//已经退款
			return true;
		}

		//4、如若未完成退款，调用微信端接口实现退款
		String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";

		Map<String, String> map = new HashMap<>();
		map.put("appid", ConstantPropertiesUtils.APPID);
		map.put("mch_id", ConstantPropertiesUtils.PARTNER);
		map.put("nonce_str", WXPayUtil.generateNonceStr());
		map.put("out_trade_no", paymentInfo.getOutTradeNo());
		map.put("out_refund_no", "tk_" + paymentInfo.getOutTradeNo());//退款单号
		map.put("total_fee", "1");
		map.put("refund_fee", "1");

		try {
			String s = WXPayUtil.generateSignedXml(map, ConstantPropertiesUtils.PARTNERKEY);

			HttpClient httpClient = new HttpClient(url);
			httpClient.setXmlParam(s);
			httpClient.setHttps(true);
			httpClient.setCert(true);
			httpClient.setCertPassword(ConstantPropertiesUtils.PARTNER);
			httpClient.post();

			//5、微信端的返回值
			String content = httpClient.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

			//6、判断微信端退款接口是否调用成功
			String result_code = resultMap.get("result_code");//SUCCESS
			if ("SUCCESS".equalsIgnoreCase(result_code)) {
				//7、微信端退款接口调用成功，修改退款记录
				refundInfo.setTradeNo(resultMap.get("transaction_id"));//退款流水号
				refundInfo.setCallbackTime(new Date());
				refundInfo.setCallbackContent(resultMap.toString());
				refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());// 2
				refundInfoService.updateById(refundInfo);//根据id进行修改
//                refundInfoService.save(refundInfo);添加

				//  Mongodb ：   save--》 有id--》修改；  没有id--》新增
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
