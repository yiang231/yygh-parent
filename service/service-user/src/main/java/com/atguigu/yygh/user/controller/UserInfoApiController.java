package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.AuthContextHolder;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(value = "用户接口")
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

	@ApiOperation(value = "用户认证接口")
	@PostMapping("auth/userAuth")
	public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
		//从request请求头中获取令牌
		Long id = AuthContextHolder.getUserId(request);
		userInfoService.userAuth(id, userAuthVo);
		return R.ok();
	}

	@ApiOperation(value = "用户认证时得到对应的用户接口")
	@GetMapping("auth/getUserInfo")
	public R getUserInfo(HttpServletRequest request) {
		//获取userId，得到对应的用户
		Long userId = AuthContextHolder.getUserId(request);
		UserInfo userInfo = userInfoService.getById(userId);

		//设置认证状态
		Integer authStatus = userInfo.getAuthStatus();
		//将认证状态进行转化
		String authStatusString = AuthStatusEnum.getStatusNameByStatus(authStatus);
		/*AuthStatusEnum[] values = AuthStatusEnum.values();
		for (AuthStatusEnum value : values) {
			Integer status = value.getStatus();
			if (authStatus == status) {
				String name = value.getName();
			}
		}*/
		userInfo.getParam().put("authStatusString", authStatusString);
		return R.ok().data("userInfo", userInfo);
	}
}
