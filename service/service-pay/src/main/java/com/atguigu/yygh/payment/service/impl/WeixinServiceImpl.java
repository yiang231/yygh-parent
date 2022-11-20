package com.atguigu.yygh.payment.service.impl;

import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.orders.client.OrdersFeignClient;
import com.atguigu.yygh.payment.service.PaymentInfoService;
import com.atguigu.yygh.payment.service.WeixinService;
import com.atguigu.yygh.payment.util.ConstantPropertiesUtils;
import com.atguigu.yygh.payment.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
}
