package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(value = "用户登录接口")
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {
	@Autowired
	private UserInfoService userInfoService;

	@ApiOperation(value = "用户登录或者是注册")
	@PostMapping("login")
	public R login(@RequestBody LoginVo loginVo) {
		/*name = 界面右上角现实的文字
		 *token = 表示用户当前的身份 */
		Map<String, Object> map = userInfoService.login(loginVo);
		return R.ok().data(map);
	}
}
