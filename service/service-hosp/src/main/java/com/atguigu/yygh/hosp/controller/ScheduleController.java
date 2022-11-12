package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "医院排班接口")
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {
	@Autowired
	private ScheduleService scheduleService;

	/**
	 * 根据医院编号+科室编号+分页  查询日期列表
	 *
	 * @param page
	 * @param limit
	 * @param hoscode
	 * @param depcode
	 * @return
	 */
	@ApiOperation(value = "查询科室列表")
	@GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
	public R getScheduleRule(@ApiParam(name = "page", value = "医院科室列表当前页") @PathVariable long page,
							 @ApiParam(name = "limit", value = "医院科室列表总页数") @PathVariable long limit,
							 @ApiParam(name = "hoscode", value = "根据 hoscode,depcode 查询医院科室列表") @PathVariable String hoscode,
							 @ApiParam(name = "depcode", value = "根据 hoscode,depcode 查询医院科室列表") @PathVariable String depcode) {
		Map<String, Object> map = scheduleService.getScheduleRule(page, limit, hoscode, depcode);
		return R.ok().data(map);// { code,success,message,data:{total,bookingScheduleRuleList,baseMap:{hosname:''}  } }
	}

	/**
	 * 查询排班详情
	 *
	 * @param hoscode
	 * @param depcode
	 * @param workDate
	 * @return
	 */
	@ApiOperation(value = "查询某个时间排班详情")
	@GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
	public R getScheduleDetail(@ApiParam(name = "hoscode", value = "根据 hoscode,depcode,排班日期查询当前排版日期下的排版详情列表") @PathVariable String hoscode,
							   @ApiParam(name = "depcode", value = "根据 hoscode,depcode,排班日期查询当前排版日期下的排版详情列表") @PathVariable String depcode,
							   @ApiParam(name = "workDate", value = "根据 hoscode,depcode,排班日期查询当前排版日期下的排版详情列表") @PathVariable String workDate) {
		List<Schedule> list = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
		return R.ok().data("list", list);
	}
}
