package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
	void save(Map<String, Object> paramMap);

	Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

	void remove(String hoscode, String hosScheduleId);

	// 查询页面顶部排班日期
	Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode);

	List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

	Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

	Schedule getById(String id);
}
