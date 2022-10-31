package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//医院设置接口
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {
	@Autowired
	private HospitalSetService hospitalSetService;

	//查询所有医院设置
	@GetMapping("findAll")
	public List<HospitalSet> findAll() {
		List<HospitalSet> list = hospitalSetService.list();
		return list;
	}
}
