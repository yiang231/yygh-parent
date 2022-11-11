package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private HospitalRepository hospitalRepository;

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

	// mongodb 中的聚合【分组】查询-排班页面信息
	@Override
	public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
		Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
		Aggregation aggregation = Aggregation.newAggregation( // 注意：换行时将所有的分隔符号写在下一行中，这样即使单行注释也不会报错
				Aggregation.match(criteria) // 需要对那些数据进行分组 hoscode,depcode
				, Aggregation.group("workDate") // 按照哪个属性来进行分组聚合 workDate
						.count().as("docCount") // 统计排班数量【医生数量】
						.sum("reservedNumber").as("reservedNumber") // 可预约数求和
						.sum("availableNumber").as("availableNumber") // 剩余预约数求和
						.first("workDate").as("workDate") // 每组数据中，将第一个 workDate 取出来
				, Aggregation.sort(Sort.Direction.ASC, "workDate") //排序
				, Aggregation.skip((page - 1) * limit) //分页
				, Aggregation.limit(limit)
		);
		// inputType 传进去的要统计的对象 Schedule.class，outputType 统计完成的对象 BookingScheduleRuleVo.class 每一组排班就会统计出一组 BookingScheduleRuleVo
		AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
		// 通过 getMappedResults() 方法拿到映射结果对象
		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

		bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
			// 排班日期转化方便前端展示
			bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());

			String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
			bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
		});

		// 总日期个数【前端分页展示】
		Integer total = this.getTotal(hoscode, depcode);

		// 医院名称
		String hosname = hospitalRepository.findByHoscode(hoscode).getHosname();

		// 封装数据
		Map<String, Object> result = new HashMap<>();
		result.put("total", total);
		result.put("bookingScheduleRuleList", bookingScheduleRuleVoList); // 起名不同，和前端保持一致

		Map<String, String> baseMap = new HashMap<>();
		baseMap.put("hosname", hosname);
		result.put("baseMap", baseMap);

		return result;
	}

	// 统计数据，按照 hoscode 以及 depcode 查出的排班来自多少个日期【总记录数】
	public Integer getTotal(String hoscode, String depcode) {
		// mongodb 中的聚合【分组】查询
		Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
		Aggregation aggregation = Aggregation.newAggregation( // 注意：换行时将所有的分隔符号写在下一行中，这样即使单行注释也不会报错
				Aggregation.match(criteria) // 需要对那些数据进行分组 hoscode,depcode
				, Aggregation.group("workDate")// 按照哪个属性来进行分组聚合 workDate
				//.count().as("docCount")// 统计排班数量【医生数量】
				//.sum("reservedNumber").as("reservedNumber") // 可预约数求和
				//.sum("availableNumber").as("availableNumber") // 剩余预约数求和
				//.first("workDate").as("workDate") // 每组数据中，将第一个 workDate 取出来
				//, Aggregation.sort(Sort.Direction.ASC, "workDate") //排序
				//,Aggregation.skip((page - 1) * limit), //分页
				//Aggregation.limit(limit)
		);
		// inputType 传进去的要统计的对象 Schedule.class，outputType 统计完成的对象 BookingScheduleRuleVo.class 每一组排班就会统计出一组 BookingScheduleRuleVo
		AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
		// 通过 getMappedResults() 方法拿到映射结果对象
		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();
		int total = bookingScheduleRuleVoList.size(); // 总记录数即查询到的当天的排班数量
		/*bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
			bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());

			String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
			bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
		});*/
		return total;
	}

	// 前端传来的日期是字符串类型，需要进行转换
	@Override
	public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
		Date date = new DateTime(workDate).toDate();
		List<Schedule> list = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);
		return list;
	}

	public String getDayOfWeek(DateTime dateTime) {
		String dayOfWeek = "";
		switch (dateTime.getDayOfWeek()) {
			case DateTimeConstants.SUNDAY:
				dayOfWeek = "周日";
				break;
			case DateTimeConstants.MONDAY:
				dayOfWeek = "周一";
				break;
			case DateTimeConstants.TUESDAY:
				dayOfWeek = "周二";
				break;
			case DateTimeConstants.WEDNESDAY:
				dayOfWeek = "周三";
				break;
			case DateTimeConstants.THURSDAY:
				dayOfWeek = "周四";
				break;
			case DateTimeConstants.FRIDAY:
				dayOfWeek = "周五";
				break;
			case DateTimeConstants.SATURDAY:
				dayOfWeek = "周六";
			default:
				break;
		}
		return dayOfWeek;
	}
}
