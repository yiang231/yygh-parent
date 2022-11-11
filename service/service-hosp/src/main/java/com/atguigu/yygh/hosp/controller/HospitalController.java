package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {
	@Autowired
	private HospitalService hospitalService;

	@ApiOperation(value = "获取分页列表")
	@PostMapping("/{page}/{limit}")
	//或者使用GetMapping，去除RequestBody注解，配合前端的get请求
	public R pageList(@ApiParam(name = "page", value = "当前页", required = true) @PathVariable Integer page,
					  @ApiParam(name = "limit", value = "总页数", required = true) @PathVariable Integer limit,
					  @ApiParam(name = "hospitalQueryVo", value = "部分Hospital信息用于查询", required = true) @RequestBody HospitalQueryVo hospitalQueryVo) {
		//分页
		Page<Hospital> pageResult = hospitalService.selectPage(page, limit, hospitalQueryVo);
		return R.ok().data("pages", pageResult);
	}

	@ApiOperation(value = "更新医院上线状态")
	@GetMapping("/updateStatus/{id}/{status}")
	public R updateStatus(@ApiParam(name = "id", value = "mongo中医院的id", required = true) @PathVariable String id,
						  @ApiParam(name = "status", value = "医院状态，1代表已上线，0代表未上线", required = true) @PathVariable Integer status) {
		hospitalService.updateStatus(id, status);
		return R.ok();
	}

	@ApiOperation(value = "从mongodb中查询医院列表")
	@GetMapping("/show/{id}")
	public R show(@ApiParam(name = "page", value = "当前页", required = true) @PathVariable("id") String id) {
		Hospital hospital = hospitalService.show(id);
		BookingRule bookingRule = hospital.getBookingRule();

		Map<String, Object> map = new HashMap<>();
		map.put("hospital", hospital);
		map.put("bookingRule", bookingRule);

		return R.ok().data("hospital", map);
	}
}
