package com.atguigu.yygh.common.excp;

import com.atguigu.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//统一异常处理器
//@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	//处理全局异常
	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public R error(Exception exp) {
		log.info("出现了异常");
		return R.error().message(exp.getMessage());
	}

	//处理特定异常
	@ResponseBody
	@ExceptionHandler(value = ArithmeticException.class)
	public R error(ArithmeticException exp) {
		log.warn("ArithmeticException");
		return R.error().message(exp.getMessage());
	}

	//处理自定义异常
	@ResponseBody
	@ExceptionHandler(value = YyghException.class)
	public R error(YyghException exp) {
		log.error(exp.getMsg());
		return R.error().message(exp.getMsg());
	}
}
