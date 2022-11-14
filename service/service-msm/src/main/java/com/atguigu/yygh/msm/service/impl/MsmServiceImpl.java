package com.atguigu.yygh.msm.service.impl;

import com.atguigu.yygh.common.utils.HttpUtils;
import com.atguigu.yygh.msm.service.MsmService;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {
	//短信平台服务端的代码
	@Override
	public Boolean send(String phone, String code) {
		String host = "https://cxwg.market.alicloudapi.com";
		String path = "/sendSms";
		String method = "POST";
		String appcode = "32d7431a13d4438cab96470b75e2a6c6";//开通服务后 买家中心-查看AppCode
		Map<String, String> headers = new HashMap<String, String>();
		//最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		headers.put("Authorization", "APPCODE " + appcode);
		Map<String, String> querys = new HashMap<String, String>();
		String content = "【狗屎蛋】你的验证码是" + code + "，1分钟内有效";
		querys.put("content", content);
		querys.put("mobile", phone);
		Map<String, String> bodys = new HashMap<String, String>();

		try {
			/**
			 * 重要提示如下:
			 * HttpUtils请从
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
			 * 下载
			 *
			 * 相应的依赖请参照
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
			 */
			HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
			System.out.println(response.toString());
			//获取response的body
			//System.out.println(EntityUtils.toString(response.getEntity()));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
