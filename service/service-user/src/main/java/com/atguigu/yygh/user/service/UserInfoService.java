package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
	Map<String, Object> login(LoginVo loginVo);

	UserInfo selectWxByOpenId(String openid);

	UserInfo selectUserByPhone(String phone);

	void userAuth(Long userId, UserAuthVo userAuthVo);

	//后台管理用户列表
	Page<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

	void lock(Long userId, Integer status);

	/*
    用户详情 （用户+就诊人）
     */
	Map<String, Object> show(Long userId);
}
