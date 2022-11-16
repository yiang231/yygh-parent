package com.atguigu.yygh.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Api(value = "微信登陆接口")
@Controller//使用RestController注解callback方法就拿不到数据
@RequestMapping("/api/user/wx")
public class WeixinApiController {
	@Autowired
	UserInfoService userInfoService;

	@ApiOperation(value = "从微信接口中获取用户信息")
	@GetMapping("getLoginParam")
	@ResponseBody
	public R getLoginParam() throws UnsupportedEncodingException {

		Map<String, Object> map = new HashMap<>();
		map.put("self_redirect", true);
		map.put("id", "weixinLogin");
		map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
		map.put("scope", "snsapi_login");
		map.put("redirect_uri", URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "utf-8"));
		String state = System.currentTimeMillis() + "";
		map.put("state", state);//二维码
		//session.setAttribute("state", state);//不需要就不传  HttpSession session 跨站请求伪造
		map.put("style", "black");
		map.put("href", "");

		return R.ok().data(map);
	}

	@ApiOperation(value = "扫码登录之后点击确认按钮，执行这个回调方法重定向")
	@GetMapping("callback")
	public String callBack(String code) throws Exception {
		//1、获取临时票据 （ 扫码之后点确认，微信开放平台端自动接口重定向，并且自动传递临时票据 ）
		System.out.println("临时票据:code = " + code);

		//2、调用微信端接口，根据code，查询当前微信用户的openid
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
				"appid=" + ConstantPropertiesUtil.WX_OPEN_APP_ID +
				"&secret=" + ConstantPropertiesUtil.WX_OPEN_APP_SECRET +
				"&code=" + code +
				"&grant_type=authorization_code";

		String s = HttpClientUtils.get(url);
		JSONObject jsonObject = JSONObject.parseObject(s);

		String access_token = jsonObject.getString("access_token");//查询微信昵称时使用
		String openid = jsonObject.getString("openid");

		//3、从user_info表中查询微信用户
		UserInfo userInfo = userInfoService.selectWxByOpenId(openid);

		//4、微信用户在userinfo表中不存在，自动注册
		if (userInfo == null) {

			//5、调用微信端接口，根据openid获取微信用户昵称
			String url_nick = "https://api.weixin.qq.com/sns/userinfo?" +
					"access_token=" + access_token +
					"&openid=" + openid;
			String result = HttpClientUtils.get(url_nick);
			JSONObject obj = JSONObject.parseObject(result);
			String nickname = obj.getString("nickname");

			//6、自动注册
			userInfo = new UserInfo();
			userInfo.setOpenid(openid);
			userInfo.setNickName(nickname);//微信昵称

			userInfoService.save(userInfo);
		}

		String name = userInfo.getName();
		if (StringUtils.isEmpty(name)) {
			name = userInfo.getNickName();
			if (StringUtils.isEmpty(name)) {
				name = userInfo.getPhone();
			}
		}
		//生成令牌
		String token = JwtHelper.createToken(userInfo.getId(), name);

		//当前微信用户是否需要绑定手机号
		// name  token  openid （手机号为空时，需要绑定手机号，需要传递openid）
		//                     （手机号不为空时，不需要绑定手机号，不需要传递openid）
		//callback.vue--小      myheader.vue --- 大
		//小窗口重定向到callback.vue ,并且传递三个参数，在callback.vue（钩子方法中）中调用myheader.vue中的loginCallback方法
		return "redirect:http://localhost:3000/weixin/callback?" +
				"name=" + URLEncoder.encode(name, "UTF-8") + "&" +
				"token=" + token + "&" +
				"openid=" + (StringUtils.isEmpty(userInfo.getPhone()) ? openid : "");// pages/weixin/callback.vue
	}
}
