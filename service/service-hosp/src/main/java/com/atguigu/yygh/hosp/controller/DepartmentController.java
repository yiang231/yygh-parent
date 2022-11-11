package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "医院科室接口")
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {
	@Autowired
	private DepartmentService departmentService;

	@ApiOperation(value = "查询科室列表")
	@GetMapping("getDeptList/{hoscode}")
	public R getDeptList(@ApiParam(name = "hoscode", value = "根据hoscode查询科室列表") @PathVariable String hoscode) {
		List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
		return R.ok().data("list", list);
	}
}
