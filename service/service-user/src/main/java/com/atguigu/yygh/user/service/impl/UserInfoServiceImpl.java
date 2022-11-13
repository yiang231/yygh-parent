package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.excp.YyghException;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
	@Override
	public Map<String, Object> login(LoginVo loginVo) {
		//1、手机号和验证码是否为空
		//手机号+验证码的登录逻辑
		//页面上输入的手机号+短信验证码
		String phone = loginVo.getPhone();
		String code = loginVo.getCode();
		//参数校验 null
		if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
			throw new YyghException(20001, "手机号和验证码不能为空");
		}

		//2、校验验证码（自动注册之前进行） 伪代码
		//TODO
//        if(!code.equalsIgnoreCase("从redis中根据phone获取真实的验证码")){
//            throw new YyghException(20001,"验证码不正确");
//        }


		//3、判断该用户是否是新用户（手机号在数据库中是否存在，不存在，直接注册）
		//从mysql中根据手机号查询
		QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("phone", phone);
		UserInfo userInfo = baseMapper.selectOne(queryWrapper);

		//4、新用户，直接注册
		if (userInfo == null) {
			userInfo = new UserInfo();

			userInfo.setPhone(phone);
			userInfo.setStatus(1);//正常状态
			userInfo.setAuthStatus(0);//认证状态-未认证
			userInfo.setCreateTime(new Date());
			userInfo.setUpdateTime(new Date());

			baseMapper.insert(userInfo);
		}

		//5、判断用户的状态是否被锁定
		if (userInfo.getStatus() == 0) {
			throw new YyghException(20001, "用户被锁定，不能登录");
		}

		//6、封装返回值
		String name = "";
		name = userInfo.getName();//真实姓名
		if (StringUtils.isEmpty(name)) {
			name = userInfo.getNickName();//微信昵称
			if (StringUtils.isEmpty(name)) {
				name = userInfo.getPhone();
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("name", name);//右上角显示的名字
		map.put("token", "");//用户的令牌

		return map;
	}
}
