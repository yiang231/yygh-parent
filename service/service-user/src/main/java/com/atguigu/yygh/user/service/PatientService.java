package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PatientService extends IService<Patient> {
	//获取就诊人列表
	List<Patient> findListByUserId(Long userId);

	//根据id获取就诊人
	Patient getPatientById(Long id);
}
