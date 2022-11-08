package com.atguigu.yygh.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "通用的返回结果")
@Data
public class R {
	@ApiModelProperty(value = "自定义返回状态码", example = "20000")
	private Integer code;//20000成功  20001失败
	private String message;//操作成功  操作失败
	private boolean success;//true false
	private Map<String, Object> data = new HashMap<>(); //存放数据

	public static R ok() {
		R r = new R();
		r.setCode(ResultCode.SUCCESS);
		r.setMessage("操作成功");
		r.setSuccess(true);
		return r;
	}

	public static R error() {
		R r = new R();
		r.setCode(ResultCode.ERROR);
		r.setMessage("操作失败");
		r.setSuccess(false);
		return r;
	}

	public static void main(String[] args) {
		R r = R.ok();
		System.out.println("result = " + r);
		R newCode = r.code(11111);
		System.out.println("newCode = " + newCode);
	}

	//修改code的值
	public R code(Integer code) {
		this.setCode(code);
		return this;
	}

	public R message(String message) {
		this.setMessage(message);
		return this;
	}

	public R success(boolean success) {
		this.setSuccess(success);
		return this;
	}

	//手动设置数据
	public R data(String key, Object value) {
		this.data.put(key, value);
		return this;
	}

	//完全修改数据
	public R data(Map<String, Object> data) {
		this.setData(data);
		return this;
	}

	//追加数据
	public R dataAppend(Map<String, Object> data) {
		this.data.putAll(data);
		return this;
	}
}
