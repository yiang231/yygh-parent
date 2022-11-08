package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/api/hosp")
public class ApiController {
	@Autowired
	private HospitalService hospitalService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private ScheduleService scheduleService;

	@PostMapping("/saveHospital")
	public Result saveHospital(HttpServletRequest request) {
		// 新增或者修改
		hospitalService.save(toMap(request));
		return Result.ok();//code = 200, data = null, message = 成功
	}

	@PostMapping("/hospital/show")
	public Result hospitalShow(HttpServletRequest request) {
		//查询医院列表
		Hospital hospital = hospitalService.getByHoscode(toMap(request));
		return Result.ok(hospital);
	}

	@PostMapping("/saveDepartment")
	public Result saveDepartment(HttpServletRequest request) {
		//添加科室
		departmentService.save(toMap(request));
		return Result.ok();
	}

	@PostMapping("/department/list")
	public Result departmentList(HttpServletRequest request) {
		Map<String, Object> parmaMap = toMap(request);
		String hoscode = (String) parmaMap.get("hoscode");
		String page = (String) parmaMap.get("page");
		String limit = (String) parmaMap.get("limit");

		//查询条件
		DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
		departmentQueryVo.setHoscode(hoscode);
		Page<Department> pageResult = departmentService.selectPage(Integer.parseInt(page), Integer.parseInt(limit), departmentQueryVo);
		return Result.ok(pageResult);
	}

	@PostMapping("department/remove")
	public Result departmentRemove(HttpServletRequest request) {
		Map<String, Object> parmaMap = toMap(request);
		String hoscode = (String) parmaMap.get("hoscode");
		String depcode = (String) parmaMap.get("depcode");
		departmentService.remove(hoscode, depcode);
		return Result.ok();
	}

	@PostMapping("saveSchedule")
	public Result saveSchedule(HttpServletRequest request) {
		Map<String, Object> paramMap = toMap(request);
		scheduleService.save(paramMap);
		return Result.ok();
	}

	@PostMapping("/schedule/list")
	public Result scheduleList(HttpServletRequest request) {
		Map<String, Object> parmaMap = toMap(request);
		String hoscode = (String) parmaMap.get("hoscode");
		String depcode = (String) parmaMap.get("depcode");
		String page = (String) parmaMap.get("page");
		String limit = (String) parmaMap.get("limit");

		//查询条件
		ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
		scheduleQueryVo.setHoscode(hoscode);
		scheduleQueryVo.setDepcode(depcode);
		Page<Schedule> pageResult = scheduleService.selectPage(Integer.parseInt(page), Integer.parseInt(limit), scheduleQueryVo);
		return Result.ok(pageResult);
	}

	@PostMapping("/schedule/remove")
	public Result scheduleRemove(HttpServletRequest request) {
		Map<String, Object> parmaMap = toMap(request);
		String hoscode = (String) parmaMap.get("hoscode");
		String hosScheduleId = (String) parmaMap.get("hosScheduleId");
		scheduleService.remove(hoscode, hosScheduleId);
		return Result.ok();
	}

	public Map<String, Object> toMap(HttpServletRequest request) {
		// 1、获取所有的参数
		Map<String, String[]> parameterMap = request.getParameterMap();
		// 2、转换Map
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);

		return paramMap;
	}
}
