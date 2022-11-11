package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class DepartmentTest {
	@Autowired
	DepartmentService departmentService;
	@Autowired
	ScheduleRepository scheduleRepository;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Test
	public void test1() {
		List<DepartmentVo> deptTree = departmentService.findDeptTree("10000");
		deptTree.forEach(System.out::println);
	}

	@Test
	//使用 Java-Api 进行排班的【聚合】查询
	public void test2() {
		String hoscode = "10000";
		String depcode = "200040878";

		// 查询该科室下所有的排班
		List<Schedule> scheduleList = scheduleRepository.findByHoscodeAndDepcode(hoscode, depcode);
		// 用于封装数据
		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
		// 统计这些排班来自于哪些日期 类似 findDeptTree 的写法
		Map<Date, List<Schedule>> collect = scheduleList.stream().collect(Collectors.groupingBy(Schedule::getWorkDate));
		collect.forEach((key, value) -> {
			// k 所有不同的日期 WorkDate,v 对应日期下所有的排班
			String dayOfWeek = getDayOfWeek(new DateTime(key));// 星期几
			Integer reservedNumberSum = 0;
			Integer availableNumberSum = 0;
			for (Schedule schedule : value) {
				Integer reservedNumber = schedule.getReservedNumber();// 可预约数
				Integer availableNumber = schedule.getAvailableNumber();// 剩余预约数
				reservedNumberSum += reservedNumber;
				availableNumberSum += availableNumber;
			}
			System.out.println(dayOfWeek + "===" + availableNumberSum + "/" + reservedNumberSum + "===" + collect.size());

			// 封装数据
			BookingScheduleRuleVo bookingScheduleRuleVo = new BookingScheduleRuleVo();
			bookingScheduleRuleVo.setWorkDate(key);
			bookingScheduleRuleVo.setWorkDateMd(key);
			bookingScheduleRuleVo.setDocCount(collect.size());// 就诊医生人数【排班数量】
			bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
			bookingScheduleRuleVo.setAvailableNumber(availableNumberSum);
			bookingScheduleRuleVo.setReservedNumber(reservedNumberSum);

			bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
		});
	}

	/*String getDayOfWeek(DateTime dateTime) {
		List<String> strings = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
		int dayOfWeek = dateTime.getDayOfWeek();
		return strings.get(dayOfWeek - 1);
	}*/

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

	@Test
	public void testWeekOfDay() {
		List<String> list = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");

		Date date = new Date();
		DateTime dateTime = new DateTime(date);

		int dayOfWeek = dateTime.getDayOfWeek();
		System.out.println("dayOfWeek = " + dayOfWeek);// 当天星期数

		System.out.println(list.get(dayOfWeek - 1));
	}

	@Test
	public void getDayOfWeek() {
		String dayOfWeek = "";
		Date date = new Date();
		DateTime dateTime = new DateTime(date);
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
		System.out.println("今天是" + dayOfWeek);
	}

	// getScheduleRule接口测试
	@Test
	public void test4() {
		Map<String, Object> map = scheduleService.getScheduleRule(1, 2, "10000", "200040878");
		map.forEach((key, value) -> {
			System.out.println("key = " + key);
			System.out.println("value = " + value);
		});
	}

	// mongoTemplate聚合查询总记录数
	@Test
	public void test5() {
		String hoscode = "10000";
		String depcode = "200040878";
		// mongodb 中的聚合【分组】查询
		Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
		Aggregation aggregation = Aggregation.newAggregation(
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

		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();
		int size = bookingScheduleRuleVoList.size(); // 总记录数
		System.out.println("size = " + size);
		/*bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
			bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());

			String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
			bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
		});*/
	}
}
