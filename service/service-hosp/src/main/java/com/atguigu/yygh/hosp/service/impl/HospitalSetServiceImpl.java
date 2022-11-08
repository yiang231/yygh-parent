package com.atguigu.yygh.hosp.service.impl;

import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
	@Autowired
	private HospitalSetMapper hospitalSetMapper;

	@Override
	public Map<String, Object> selectPage(Long page, Long limit) {
		Page<HospitalSet> pageParma = new Page<>(page, limit);
		//以下两种都可以实现分页查询
		//this.page(pageParma);
		//hospitalSetMapper.selectPage(pageParma, null);
		//不需要注入HospitalSetMapper的方法
		baseMapper.selectPage(pageParma, null);

		List<HospitalSet> rows = pageParma.getRecords();//当前页结果集
		long total = pageParma.getTotal();//总记录数

		Map<String, Object> map = new HashMap<>();
		map.put("total", total);
		map.put("rows", rows);
		return map;
	}

	@Override
	public HospitalSet findByHoscode(String hoscode) {
		QueryWrapper<HospitalSet> hospitalSetQueryWrapper = new QueryWrapper<>();
		hospitalSetQueryWrapper.eq("hoscode", hoscode);
		//HospitalSet hospitalSet = HospitalSetService.getOne(hospitalSetQueryWrapper);
		//HospitalSet hospitalSet = this.getOne(hospitalSetQueryWrapper);//当前对象
		//baseMapper.selectOne(hospitalSetQueryWrapper);
		return this.getOne(hospitalSetQueryWrapper);
	}
}
