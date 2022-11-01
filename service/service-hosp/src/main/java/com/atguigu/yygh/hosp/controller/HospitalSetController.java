package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(description = "医院设置接口")//加在类上
@CrossOrigin//设置跨域请求
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {
	@Autowired
	private HospitalSetService hospitalSetService;

	@ApiOperation(value = "分页条件医院设置列表")
	@PostMapping("{page}/{limit}")//条件分页查询
	public Result pageQuery(@ApiParam(name = "page", value = "第几页", required = true) @PathVariable Long page,
							@ApiParam(name = "limit", value = "每页条数", required = true) @PathVariable Long limit,
							@RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
		Page<HospitalSet> hospitalSetPage = new Page<>(page, limit);

		QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
		if (hospitalSetQueryVo == null) {
			//查询所有
			hospitalSetService.page(hospitalSetPage, queryWrapper);
		} else {
			//条件查询
			String hoscode = hospitalSetQueryVo.getHoscode();
			String hosname = hospitalSetQueryVo.getHosname();
			if (!StringUtils.isEmpty(hoscode)) {
				queryWrapper.eq("hoscode", hoscode);
			}
			if (!StringUtils.isEmpty(hosname)) {
				queryWrapper.like("hosname", hosname);
			}
			hospitalSetService.page(hospitalSetPage, queryWrapper);
		}
		//返回值
		long total = hospitalSetPage.getTotal();
		List<HospitalSet> rows = hospitalSetPage.getRecords();
		return Result.ok().data("total", total).data("rows", rows);
	}

	@ApiOperation(value = "医院设置无条件分页查询")
	@GetMapping("{page}/{limit}")//无条件分页查询
	public Result pageList(@ApiParam(name = "page", value = "第几页", required = true) @PathVariable Long page,
						   @ApiParam(name = "limit", value = "每页条数", required = true) @PathVariable Long limit) {
		/*Page<HospitalSet> pageParma = new Page<>(page, limit);
		hospitalSetService.page(pageParma);
		List<HospitalSet> rows = pageParma.getRecords();//当前页结果集
		long total = pageParma.getTotal();//总记录数*/
		Map<String, Object> map = hospitalSetService.selectPage(page, limit);
		return Result.ok().data(map);
	}

	//查询所有医院设置
	@ApiOperation(value = "医院设置findAll")//加在方法上
	@GetMapping("findAll")
	public Result findAll() {
		List<HospitalSet> list = hospitalSetService.list();
		try {
			int i = 1 / 0;
		} catch (Exception e) {
			throw new RuntimeException("出现了自定义异常");
		}
		return Result.ok().data("list", list);
	}

	//逻辑删除
	@ApiOperation(value = "医院设置根据id逻辑删除,PathVariable")
	@DeleteMapping("{id}")
	//@ApiParam加载参数中
	private Result removeById(@ApiParam(name = "id", value = "医院设置主键", required = true) @PathVariable String id) {
		boolean bool = hospitalSetService.removeById(id);
		return bool ? Result.ok() : Result.error();
	}

	//医院设置新增
	@PostMapping("save")
	@ApiOperation(value = "开通医院设置")
	public Result save(@ApiParam(name = "hospitalSet", value = "医院设置要新增的对象") @RequestBody HospitalSet hospitalSet) {
		//1、需要判断是否存在医院编号hoscode，即判断是否开通医院设置
		String hoscode = hospitalSet.getHoscode();
		if (StringUtils.isEmpty(hoscode)) {
			return Result.error().message("医院编号不能为空");
		}
		//2、hoscode具有唯一性，不能重复。方案：先带医院编号查询再新增
		QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("hoscode", hoscode);
		int count = hospitalSetService.count(queryWrapper);//查询符合条件的总数
		if (count >= 1) {
			return Result.error().message("医院设置已经开通，不可重复设置");
		}
		//3、将医院设置状态设置为1，非锁定状态
		hospitalSet.setStatus(1);
		//4、新增数据
		boolean bool = hospitalSetService.save(hospitalSet);
		return bool ? Result.ok().message("开通成功") : Result.error().message("开通失败");
	}

	@GetMapping("getHospSet")
	@ApiOperation(value = "医院设置根据id查询getById,RequestParam")
	public Result getById(@ApiParam(name = "id", value = "医院设置查询的主键", required = true) @RequestParam Long id) {
		HospitalSet hospitalSet = hospitalSetService.getById(id);
		return Result.ok().data("item", hospitalSet);
	}

	@ApiOperation(value = "医院设置更新数据")
	@PostMapping("updateHospSet")
	public Result updateById(@ApiParam(name = "hospitalSet", value = "医院设置更新的对象", required = true) @RequestBody HospitalSet hospitalSet) {
		Long hospitalSetId = hospitalSet.getId();
		if (StringUtils.isEmpty(hospitalSetId)) {
			return Result.ok().message("id不能为空");
		}
		//传过去带医院编号的数据【不可被修改】，手动将hoscode改为空，由于update时动态SQL的特点，确保hoscode不会被修改
		hospitalSet.setHoscode(null);
		//更新数据
		hospitalSetService.updateById(hospitalSet);
		return Result.ok();
	}
}
