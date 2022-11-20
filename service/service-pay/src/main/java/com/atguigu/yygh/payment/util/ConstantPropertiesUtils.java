package com.atguigu.yygh.payment.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantPropertiesUtils implements InitializingBean {
	public static String APPID;
	public static String PARTNER;
	public static String PARTNERKEY;
	public static String CERT;
	@Value("${weixin.cert}")//证书不要放在项目中会无法识别路径
	private String cert;
	@Value("${weixin.pay.appid}")
	private String appid;
	@Value("${weixin.pay.partner}")
	private String partner;
	@Value("${weixin.pay.partnerkey}")
	private String partnerkey;

	@Override
	public void afterPropertiesSet() throws Exception {
		APPID = appid;
		PARTNER = partner;
		PARTNERKEY = partnerkey;
		CERT = cert;
	}
}

