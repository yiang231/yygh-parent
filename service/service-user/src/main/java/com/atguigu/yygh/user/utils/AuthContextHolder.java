package com.atguigu.yygh.user.utils;

import com.atguigu.yygh.common.utils.JwtHelper;

import javax.servlet.http.HttpServletRequest;

public class AuthContextHolder {
	public static Long getUserId(HttpServletRequest request) {
		return JwtHelper.getUserId(request.getHeader("token"));
	}

	public static String getUserName(HttpServletRequest request) {
		return JwtHelper.getUserName(request.getHeader("token"));
	}
}
