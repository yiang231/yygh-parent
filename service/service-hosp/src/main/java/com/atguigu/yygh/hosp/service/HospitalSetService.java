package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface HospitalSetService extends IService<HospitalSet> {
	public Map<String, Object> selectPage(Long page, Long limit);
}
