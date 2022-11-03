package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.result.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@CrossOrigin
@RestController
@RequestMapping("/admin/hosp")
public class UserLoginController {


	@PostMapping("/user/login")
	public Result login() {
		return Result.ok().data("token", "admin-token");
	}

	@GetMapping("/user/info")
	public Result info() {
		return Result.ok()
				.data("roles", Arrays.asList("admin"))
				.data("introduction", "我是尚医通平台管理员")
				.data("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
				.data("name", "尚医通");
	}

}
