package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface HospitalService {
	/**
	 * 上传医院信息
	 *
	 * @param paramMap
	 */
	void save(Map<String, Object> paramMap);

	Hospital getByHoscode(Map<String, Object> paramMap);

	Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

	void updateStatus(String id, Integer status);

	Hospital show(String id);
}
