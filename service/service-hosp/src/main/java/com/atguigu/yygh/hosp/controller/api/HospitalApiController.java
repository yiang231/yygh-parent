package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(value = "前端用户使用")
@RestController
@RequestMapping("api/hosp/hospital")
public class HospitalApiController {
	@Autowired
	private HospitalService hospitalService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private ScheduleService scheduleService;

	@ApiOperation(value = "首页查询医院列表")
	@GetMapping("{page}/{limit}")
	public R index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
		Page<Hospital> hospitalPage = hospitalService.selectPage(page, limit, hospitalQueryVo);
		return R.ok().data("pages", hospitalPage);
	}

	@ApiOperation(value = "根据医院名称进行模糊查询")
	@GetMapping("findByHosname/{hosname}")
	public R findByHosname(@PathVariable String hosname) {
		List<Hospital> list = hospitalService.findByHosname(hosname);
		return R.ok().data("list", list);
	}

	@ApiOperation(value = "首页上选中某个医院后被调用，用于查询医院详情")
	@GetMapping("{hoscode}")
	public R item(@PathVariable String hoscode) {
		Map<String, Object> map = hospitalService.item(hoscode);
		return R.ok().data(map);
	}

	@ApiOperation(value = "在详情页面根据医院编号查询医院科室列表")
	@GetMapping("department/{hoscode}")
	public R department(@PathVariable String hoscode) {
		List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
		return R.ok().data("list", list);
	}

	//医院详情页点击小科室跳转到挂号详情页面，调用该接口
	//日期分页
	@ApiOperation(value = "查询日期分页")
	@GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
	public R getBookingSchedule(@PathVariable Integer page
			, @PathVariable Integer limit
			, @PathVariable String hoscode
			, @PathVariable String depcode) {
		// 日期分页的每一块
		Map<String, Object> map = scheduleService.getBookingSchedule(page, limit, hoscode, depcode);
		return R.ok().data(map);
	}

	@ApiOperation(value = "查询排班列表")
	@GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
	public R getScheduleDetail(@PathVariable String hoscode,
							   @PathVariable String depcode,
							   @PathVariable String workDate) {
		List<Schedule> scheduleList = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
		return R.ok().data("scheduleList", scheduleList);
	}

	//点击剩余按钮，查询排班详情
	@ApiOperation(value = "查询排班详情")
	@GetMapping("getSchedule/{id}")
	public R getSchedule(@PathVariable String id) {
		Schedule schedule = scheduleService.getById(id);
		return R.ok().data("schedule", schedule);
	}

	@ApiOperation(value = "获取医院端排班scheduleOrderVo对象")
	@GetMapping("inner/getScheduleOrderVo/{scheduleId}")
	public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
		return scheduleService.getScheduleOrderVo(scheduleId);
	}
}
