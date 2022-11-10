package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(description = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
@CrossOrigin
public class HospitalController {
	@Autowired
	private HospitalService hospitalService;

	@ApiOperation(value = "获取分页列表")
	@PostMapping("/{page}/{limit}")
	public R pageList(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody HospitalQueryVo hospitalQueryVo) {
		//分页
		Page<Hospital> pageResult = hospitalService.selectPage(page, limit, hospitalQueryVo);
		return R.ok().data("pages", pageResult);
	}

	@GetMapping("/updateStatus/{id}/{status}")
	public R updateStatus(@PathVariable String id, @PathVariable Integer status) {
		hospitalService.updateStatus(id, status);
		return R.ok();
	}

	@GetMapping("/show/{id}")
	public R show(@PathVariable("id") String id) {
		Hospital hospital = hospitalService.show(id);
		BookingRule bookingRule = hospital.getBookingRule();

		Map<String, Object> map = new HashMap<>();
		map.put("hospital", hospital);
		map.put("bookingRule", bookingRule);

		return R.ok().data("hospital", map);
	}
}
