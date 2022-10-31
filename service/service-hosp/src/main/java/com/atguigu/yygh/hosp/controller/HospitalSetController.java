package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(description = "医院设置接口")//加在类上
@CrossOrigin//设置跨域请求
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {
	@Autowired
	private HospitalSetService hospitalSetService;

	//查询所有医院设置
	@ApiOperation(value = "医院设置findAll")//加在方法上
	@GetMapping("findAll")
	public List<HospitalSet> findAll() {
		List<HospitalSet> list = hospitalSetService.list();
		return list;
	}

	//逻辑删除
	@ApiOperation(value = "医院设置根据id逻辑删除,PathVariable")
	@DeleteMapping("{id}")
	//@ApiParam加载参数中
	private boolean removeById(@ApiParam(name = "id", value = "医院设置主键", required = true) @PathVariable String id) {
		return hospitalSetService.removeById(id);
	}

	@GetMapping("findOne")
	@ApiOperation(value = "医院设置findOne,RequestParam")
	public HospitalSet findOne(@RequestParam Long id) {
		return hospitalSetService.getById(id);
	}
}
