package com.atguigu.yygh.common.excp;

import com.atguigu.yygh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//统一异常处理器
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	//处理全局异常
	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public Result error(Exception exp) {
		return Result.error().message(exp.getMessage());
	}

	//处理特定异常
	@ResponseBody
	@ExceptionHandler(value = ArithmeticException.class)
	public Result error(ArithmeticException exp) {
		return Result.error().message(exp.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(value = YyghException.class)
	public Result error(YyghException exp) {
		log.error(exp.getMsg());
		return Result.error().message(exp.getMessage());
	}
}
