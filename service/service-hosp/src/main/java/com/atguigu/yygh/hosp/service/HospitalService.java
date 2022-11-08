package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;

import java.util.Map;

public interface HospitalService {
	/**
	 * 上传医院信息
	 *
	 * @param paramMap
	 */
	void save(Map<String, Object> paramMap);

	Hospital getByHoscode(Map<String, Object> paramMap);
}
