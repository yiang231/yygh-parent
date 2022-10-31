package com.atguigu.yygh.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "通用的返回结果")
@Data
public class Result {
	@ApiModelProperty(value = "自定义返回状态码", example = "20000")
	private Integer code;//20000成功  20001失败
	private String message;//操作成功  操作失败
	private boolean success;//true false
	private Map<String, Object> data = new HashMap<>(); //存放数据

	public static Result ok() {
		Result result = new Result();
		result.setCode(ResultCode.SUCCESS);
		result.setMessage("操作成功");
		result.setSuccess(true);
		return result;
	}

	public static Result error() {
		Result result = new Result();
		result.setCode(ResultCode.ERROR);
		result.setMessage("操作失败");
		result.setSuccess(false);
		return result;
	}

	public static void main(String[] args) {
		Result result = Result.ok();
		System.out.println("result = " + result);
		Result newCode = result.code(11111);
		System.out.println("newCode = " + newCode);
	}

	//修改code的值
	public Result code(Integer code) {
		this.setCode(code);
		return this;
	}

	public Result message(String message) {
		this.setMessage(message);
		return this;
	}

	public Result success(boolean success) {
		this.setSuccess(success);
		return this;
	}

	//手动设置数据
	public Result data(String key, Object value) {
		this.data.put(key, value);
		return this;
	}

	//完全修改数据
	public Result data(Map<String, Object> data) {
		this.setData(data);
		return this;
	}

	//追加数据
	public Result dataAppend(Map<String, Object> data) {
		this.data.putAll(data);
		return this;
	}
}
