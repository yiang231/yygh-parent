package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(value = "后台管理系统")
@RestController
@RequestMapping("/admin/user")
public class UserController {
	@Autowired
	private UserInfoService userInfoService;

	@ApiOperation(value = "用户列表分页查询")
	@GetMapping("{page}/{limit}")
	public R list(@PathVariable Long page, @PathVariable Long limit, UserInfoQueryVo userInfoQueryVo) {
		Page<UserInfo> pageParma = new Page<>(page, limit);
		Page<UserInfo> pageModel = userInfoService.selectPage(pageParma, userInfoQueryVo);
		//pageParma = pageModel
		return R.ok().data("pageModel", pageModel);
	}

	@ApiOperation(value = "用户锁定状态改变")
	@GetMapping("lock/{userId}/{status}")
	public R lock(@PathVariable("userId") Long userId, @PathVariable("status") Integer status) {
		userInfoService.lock(userId, status);
		return R.ok();
	}

	//点击查看按钮
	@ApiOperation(value = "用户详情")
	@GetMapping("show/{userId}")
	public R show(@PathVariable Long userId) {
		Map<String, Object> map = userInfoService.show(userId);
		return R.ok().data(map);
	}

	@ApiOperation(value = "用户认证审批")
	@GetMapping("approval/{userId}/{authStatus}")
	public R approval(@PathVariable Long userId, @PathVariable Integer authStatus) {
		userInfoService.approval(userId, authStatus);
		return R.ok();
	}
}
