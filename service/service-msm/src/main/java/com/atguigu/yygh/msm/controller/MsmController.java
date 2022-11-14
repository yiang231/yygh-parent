package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.msm.service.MsmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Api(value = "给用户端发送登录验证码")
@RestController
@RequestMapping("/api/msm")
public class MsmController {
	@Autowired
	private MsmService msmService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@ApiOperation(value = "点击发送验证码进行发送")
	@GetMapping(value = "/send/{phone}")
	public R code(@PathVariable String phone) {
		//在有效期内，验证码不能重复发送
		if (StringUtils.isEmpty(phone)) return R.error().message("手机号不能为空");
		//验证码是否还在有效期
		String code_redis = stringRedisTemplate.boundValueOps(phone).get();
		if (!StringUtils.isEmpty(code_redis)) {
			return R.ok().message("验证码已发送");
		}
		//生成验证码
		String code = (long) (new Random().nextDouble() * 1000000) + "";//六位数字
		//String uuid = UUID.randomUUID().toString().replaceAll("-", "");//32位UUID
		//String code = uuid.substring(0, 6);
		//调用方法发送
		boolean send = msmService.send(phone, code);
		if (send) {
			//发送成功，验证码放到redis，设置有效时间
			stringRedisTemplate.boundValueOps(phone).set(code, 1L, TimeUnit.MINUTES);
			return R.ok().message("发送短信成功");
		} else {
			return R.error().message("发送短信失败");
		}
	}
}
