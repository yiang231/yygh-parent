package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {
	@Autowired
	private ScheduleRepository scheduleRepository;

	@Override
	public void save(Map<String, Object> paramMap) {
		Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);
		String hosScheduleId = schedule.getHosScheduleId();
		String hoscode = schedule.getHoscode();
		Schedule schedule_mongo = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

		if (schedule_mongo == null) {
			schedule.setCreateTime(new Date());
		} else {
			schedule.setId(schedule_mongo.getId());
		}
		schedule.setUpdateTime(new Date());
		scheduleRepository.save(schedule);
	}

	@Override
	public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
		//分页mongo从1开始，倒序
		PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createTime"));
		//拷贝数据
		Schedule schedule = new Schedule();
		BeanUtils.copyProperties(scheduleQueryVo, schedule);

		//模糊查询
		ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
		Example<Schedule> example = Example.of(schedule, exampleMatcher);

		Page<Schedule> pageResult = scheduleRepository.findAll(example, pageable);

		return pageResult;
	}

	@Override
	public void remove(String hoscode, String hosScheduleId) {
		Schedule schedule = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
		if (schedule != null) {
			scheduleRepository.deleteById(schedule.getId());
		}
	}
}
