package com.atguigu.yygh.user.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.utils.AuthContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(value = "就诊人接口")
@RestController
@RequestMapping("/api/user/patient")
public class PatientController {
	@Autowired
	PatientService patientService;

	//查询就诊人列表
	@ApiOperation(value = "查询所有的就诊人")
	@GetMapping("auth/findAll")
	public R findAll(HttpServletRequest request) {
		System.out.println("request = " + request);
		Long userId = AuthContextHolder.getUserId(request);
		List<Patient> list = patientService.findListByUserId(userId);

		return R.ok().data("list", list);
	}

	//添加就诊人
	@ApiOperation(value = "添加就诊人")
	@PostMapping("auth/save")
	public R savePatient(@RequestBody Patient patient, HttpServletRequest request) {
		Long userId = AuthContextHolder.getUserId(request);
		patient.setUserId(userId);
		patientService.save(patient);
		return R.ok();
	}

	//查看就诊人详情
	@ApiOperation(value = "就诊人详情页")
	@GetMapping("auth/get/{id}")
	public R getPatient(@PathVariable Long id) {
		Patient patient = patientService.getPatientById(id);
		return R.ok().data("patient", patient);
	}

	//修改就诊人
	@ApiOperation(value = "更新就诊人")
	@PostMapping("auth/update")
	public R updatePatient(@RequestBody Patient patient) {
		patientService.updateById(patient);
		return R.ok();
	}

	//删除就诊人
	@ApiOperation(value = "删除就诊人")
	@DeleteMapping("auth/remove/{id}")
	public R removePatient(@PathVariable Long id) {
		patientService.removeById(id);
		return R.ok();
	}
}
