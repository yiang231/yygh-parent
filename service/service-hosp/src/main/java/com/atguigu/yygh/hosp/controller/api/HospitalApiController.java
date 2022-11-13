package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
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
}