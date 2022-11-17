package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private HospitalRepository hospitalRepository;
	@Autowired
	private DepartmentService departmentService;

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
						.count().as("docCount") // 统计排班数量【医生数量】，从每组中提取赋值给 BookingScheduleRuleVo 中的属性
						.sum("reservedNumber").as("reservedNumber") // 可预约数求和
						.sum("availableNumber").as("availableNumber") // 剩余预约数求和
						.first("workDate").as("workDate") // 每组数据中，将第一个 workDate 取出来赋值给 BookingScheduleRuleVo，每组中 workDate 是相同的
				, Aggregation.sort(Sort.Direction.ASC, "workDate") //排序
				, Aggregation.skip((page - 1) * limit) //分页
				, Aggregation.limit(limit)
		);
		// inputType 传进去的要统计的对象 Schedule.class，outputType 统计完成的对象 BookingScheduleRuleVo.class 每一组排班就会统计出一组 BookingScheduleRuleVo
		AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
		// 通过 getMappedResults() 方法拿到映射结果对象
		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();
		// BookingScheduleRuleVo 中其他属性赋值
		bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
			// 1、排班日期转化方便前端展示
			bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());

			String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
			bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
		});

		// 2、总日期个数【前端分页展示】
		Integer total = this.getTotal(hoscode, depcode);

		// 3、医院名称
		String hosname = hospitalRepository.findByHoscode(hoscode).getHosname();

		// 封装数据
		Map<String, Object> result = new HashMap<>();
		result.put("total", total);
		result.put("bookingScheduleRuleList", bookingScheduleRuleVoList); // 起名不同，和前端保持一致
		// 给前端进行属性封装の格式
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

	@Override
	// 得到日期的List集合
	public Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
		// 1、根据医院编号查询医院对象，预约规则
		Hospital hospital = hospitalRepository.findByHoscode(hoscode);
		String hosname = hospital.getHosname();

		BookingRule bookingRule = hospital.getBookingRule();

		// 2、私有方法 查询当前页的日期对象
		IPage<Date> iPage = this.getListData(page, limit, bookingRule);
		List<Date> pageDateList = iPage.getRecords();// 当前页的日期对象
		long pages = iPage.getPages();// 总页数
		long total = iPage.getTotal();// 总的日期个数

		//每一个日期，对应一个BookingScheduleRuleVo-->每个属性要有值
		//workDate相同的一组排班，统计一些数据，封装到BookingScheduleRuleVo对象中

		// 3、针对指定医院指定科室下的排班按照workDate分组
		Criteria criteria = Criteria.where("hoscode").is(hoscode)
				.and("depcode").is(depcode)
				.and("workDate").in(pageDateList);// 指定排班日期 日期范围之内

		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria)
				, Aggregation.group("workDate")// 按照排排班日期进行分组
						.count().as("docCount")//每一组的排班数量，赋值给BookingScheduleRuleVo中的docCount属性
						.first("workDate").as("workDate")//这一组排班中的第一个排班的workDate取出，赋值给BookingScheduleRuleVo中的workDate属性
						.sum("reservedNumber").as("reservedNumber")//这一组排班reservedNumber属性值的总和赋值给BookingScheduleRuleVo中reservedNumber属性
						.sum("availableNumber").as("availableNumber")
		);

		AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

		// 排班周期内没有排班的创建默认的ruleVo对象
		// 4、bookingScheduleRuleVoList转成map提高效率， key：workDate ， value：ruleVo对象本身 ，左边参数，右边返回值
		Map<Date, BookingScheduleRuleVo> map = bookingScheduleRuleVoList.stream().collect(Collectors.toMap(
				BookingScheduleRuleVo::getWorkDate, bookingScheduleRuleVo -> bookingScheduleRuleVo
		));
//		for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
//            Date key = bookingScheduleRuleVo.getWorkDate();
//            map.put(key,bookingScheduleRuleVo);
//        }
		//该预约周期范围内，哪些日期有排班，就有几个ruleVo对象
		List<BookingScheduleRuleVo> bookingScheduleRuleList = new ArrayList<>();// 封装下面循环得到的每一个日期对应的ruleVo对象
		//for (Date date : pageDateList) {
		for (int i = 0; i < pageDateList.size(); i++) {
			Date date = pageDateList.get(i);

			BookingScheduleRuleVo ruleVo = map.get(date);
//			BookingScheduleRuleVo ruleVo = this.getRuleVoByDate(date, bookingScheduleRuleVoList);
			if (ruleVo == null) {
				// 创建默认的ruleVo对象
				ruleVo = new BookingScheduleRuleVo();
				ruleVo.setDocCount(0);//排班的数量
				ruleVo.setWorkDate(date);
				ruleVo.setReservedNumber(-1);
				ruleVo.setAvailableNumber(-1);//没有排班
			}
			// 额外字段赋值
			ruleVo.setWorkDateMd(date);//另一种日期格式
			ruleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(date)));//星期

			// 放号状态赋值  0：正常  1：即将放号  -1：当天已停止挂号
			// 最后一页最后一条，显示即将放号1
			if (page == pages && i == pageDateList.size() - 1) {
				ruleVo.setStatus(1);
			} else {
				ruleVo.setStatus(0);
			}
			// 第一页的第一条，当天停止挂号显示-1
			if (page == 1 && i == 0) {
				String stopTime = bookingRule.getStopTime();
				DateTime dateTime = this.getDateTime(new Date(), stopTime);

				if (dateTime.isBeforeNow()) {
					ruleVo.setStatus(-1);
				}
			}
			bookingScheduleRuleList.add(ruleVo);
		}

		// 5、封装返回数据
		Map<String, Object> result = new HashMap<>();
		result.put("bookingScheduleList", bookingScheduleRuleList);
		result.put("total", total);// 总日期个数

		Map<String, Object> baseMap = new HashMap<>();//存放医院名称，科室名称，当前时间，停放号时间 【页面顶部信息】
		baseMap.put("hosname", hosname);

		Department department = departmentService.findDepartment(hoscode, depcode);
		String bigname = department.getBigname();
		String depname = department.getDepname();

		baseMap.put("bigname", bigname);
		baseMap.put("depname", depname);
		baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));//2022年11月
		baseMap.put("releaseTime", bookingRule.getReleaseTime());//放号时间
		baseMap.put("stopTime", bookingRule.getStopTime());// 停挂时间

		result.put("baseMap", baseMap);
		return result;
	}

	@Override
	public Schedule getById(String id) {
		Schedule schedule = scheduleRepository.findById(id).get();
		this.packSchedule(schedule);
		return schedule;
	}

	//添加其他参数
	private void packSchedule(Schedule schedule) {
		//查询医院名称 + 科室名称
		String hoscode = schedule.getHoscode();
		String depcode = schedule.getDepcode();

		Hospital hospital = hospitalRepository.findByHoscode(hoscode);
		Department department = departmentService.findDepartment(hoscode, depcode);

		schedule.getParam().put("hosname", hospital.getHosname());
		schedule.getParam().put("depname", department.getDepname());
		schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
	}

	//遍历获取ruleVo对象 效率低下
	private BookingScheduleRuleVo getRuleVoByDate(Date date, List<BookingScheduleRuleVo> bookingScheduleRuleVoList) {// 从pageDateList中找到date对应的ruleVo对象
		for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
			if (bookingScheduleRuleVo.getWorkDate().equals(date)) {
				// == 判断地址    Date中的equal方法判断的是两个日期对象的long类型值
				return bookingScheduleRuleVo;
			}
		}
		return null;
	}

	// 查询当前页的日期对象
	private IPage<Date> getListData(Integer page, Integer limit, BookingRule bookingRule) {
		Integer cycle = bookingRule.getCycle();// 得到预约周期
//		String releaseTime = bookingRule.getReleaseTime();// 得到放号时间
//
//		String string = new DateTime().toString("yyyy-MM-dd") + " " + releaseTime;
//		DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(string);// 将字符串时间按照一定格式转化为DateTime时间
		DateTime dateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
		if (dateTime.isBeforeNow()) {
			// 当天开始放号
			cycle += 1;
		}
		List<Date> dateList = new ArrayList<>();// 按照预约周期创建cycle个日期对象 【总的日期个数】
		// 根据预约周期cycle创建每个日期对象
		for (Integer i = 0; i < cycle; i++) {
			// 创建第i个日期
			Date date = new DateTime().plusDays(i).toDate();// 创建预约周期中的每一天对象 带有时分秒
			String yMd = new DateTime(date).toString("yyyy-MM-dd");
			Date workData = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(yMd).toDate();// 排班日期 不带时分秒
			dateList.add(workData);
		}
		// 分页
		int begin = limit * (page - 1);// 起始页1
		int end = (page - 1) * limit + limit;// 尾页7

		//最后一页可能不够每页规定的条数
		if (end > dateList.size()) {
			end = dateList.size();
		}

		List<Date> pageDataList = new ArrayList<>();// 拿到每个分页的List集合
		for (int i = begin; i < end; i++) {
			Date date = dateList.get(i);// 拿到当前页的日期对象
			pageDataList.add(date);
		}
		// 总记录数 总页数
		//int size = dateList.size();// 总记录数
		//int pages = size / limit + size % limit == 0 ? 0 : 1;// 总页数
		//使用page来计算
		IPage<Date> dataPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, dateList.size());
		dataPage.setRecords(pageDataList);// 当前页数
//		long total = dataPage.getTotal();
//		long pages = dataPage.getPages();
//		List records = dataPage.getRecords();// 当前页的日期对象
		return dataPage;
	}

	private DateTime getDateTime(Date date, String time) {
		//String releaseTime = bookingRule.getReleaseTime();// 得到放号时间
		String string = new DateTime(date).toString("yyyy-MM-dd") + " " + time;
		DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(string);// 将字符串时间按照一定格式转化为DateTime时间
		return dateTime;
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
