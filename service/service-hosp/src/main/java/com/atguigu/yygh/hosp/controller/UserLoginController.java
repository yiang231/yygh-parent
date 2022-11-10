package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Api(tags = "前端登录接口")
@CrossOrigin
@RestController
@RequestMapping("/admin/hosp")
public class UserLoginController {

	@ApiOperation(value = "前端登录")
	@PostMapping("/user/login")
	public R login() {
		return R.ok().data("token", "admin-token");
	}

	@ApiOperation(value = "前端登录信息")
	@GetMapping("/user/info")
	public R info() {
		return R.ok()
				.data("roles", Arrays.asList("admin"))
				.data("introduction", "我是尚医通平台管理员")
				.data("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
				.data("name", "尚医通");
	}
}
