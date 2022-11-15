package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.excp.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private UserInfoService userInfoService;

	@Override
	//用户登录
	public Map<String, Object> login(LoginVo loginVo) {
		String openid = loginVo.getOpenid();
		if (StringUtils.isEmpty(openid)) {
			//此时不是微信扫码登录，调用验证码登录
			return this.phoneAndCodeLogin(loginVo);
		} else {
			//微信扫码登录并且绑定手机号
			return this.weixinBindPhone(loginVo);
		}
	}

	private Map<String, Object> weixinBindPhone(LoginVo loginVo) {
		//微信扫码登陆后绑定手机号，手机号不存在则直接赋值手机号，有则将两条数据进行合并保留一条数据
		//1、根据openId以及手机号查询该用户
		UserInfo userInfoByOpenId = userInfoService.selectWxByOpenId(loginVo.getOpenid());
		UserInfo userInfoByPhone = userInfoService.selectUserByPhone(loginVo.getPhone());
		if (StringUtils.isEmpty(userInfoByPhone)) {
			//手机号不存在时
			userInfoByOpenId.setPhone(loginVo.getPhone());
		} else {
			//手机号存在时
			userInfoByOpenId.setPhone(userInfoByPhone.getPhone());
			userInfoByOpenId.setName(userInfoByPhone.getName());
			userInfoByOpenId.setCertificatesType(userInfoByPhone.getCertificatesType());
			userInfoByOpenId.setCertificatesNo(userInfoByPhone.getCertificatesNo());
			userInfoByOpenId.setCertificatesUrl(userInfoByPhone.getCertificatesUrl());
			userInfoByOpenId.setAuthStatus(userInfoByPhone.getAuthStatus());
			//先删除在更新
			baseMapper.deleteById(userInfoByPhone.getId());
			baseMapper.updateById(userInfoByOpenId);
		}
		return this.get(userInfoByOpenId);
	}

	//封装方法，手机和验证码登录
	private Map<String, Object> phoneAndCodeLogin(LoginVo loginVo) {
		//1、手机号和验证码是否为空
		//手机号+验证码的登录逻辑
		//页面上输入的手机号+短信验证码
		String phone = loginVo.getPhone();
		String code = loginVo.getCode();
		//参数校验 null
		if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
			throw new YyghException(20001, "手机号和验证码不能为空");
		}
		//2、校验验证码（自动注册之前进行）
		String code_redis = stringRedisTemplate.boundValueOps(phone).get();
		if (StringUtils.isEmpty(code_redis)) throw new YyghException(20001, "请先获取验证码");
		if (!code.equalsIgnoreCase(code_redis)) throw new YyghException(20001, "验证码不正确");

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
		if (userInfo.getStatus() == 0) throw new YyghException(20001, "用户被锁定，不能登录");

		//6、封装返回值
		return this.get(userInfo);
	}

	@Override
	public UserInfo selectWxByOpenId(String openid) {
		QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("openid", openid);
		UserInfo userInfo = baseMapper.selectOne(queryWrapper);
		return userInfo;
	}

	@Override
	public UserInfo selectUserByPhone(String phone) {
		QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
		wrapper.eq("phone", phone);
		return baseMapper.selectOne(wrapper);
	}

	private Map<String, Object> get(UserInfo userInfo) {
		Map<String, Object> map = new HashMap<>();
		String name = userInfo.getName();
		if (StringUtils.isEmpty(name)) {
			name = userInfo.getNickName();
			if (StringUtils.isEmpty(name)) {
				name = userInfo.getPhone();
			}
		}
		Long id = userInfo.getId();
		map.put("name", name);
		String token = JwtHelper.createToken(id, name);
		map.put("token", token);
		return map;
	}
}
