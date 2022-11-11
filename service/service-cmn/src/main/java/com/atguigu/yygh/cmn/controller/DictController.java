package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "数据字典接口")//加在类上
//@CrossOrigin//设置跨域请求
@RestController
@RequestMapping("/admin/cmn/dict")
public class DictController {
	@Autowired
	public DictService dictService;

	@ApiOperation(value = "根据数据id查询子数据列表")
	@GetMapping("findChildData/{id}")
	public R findChildData(@ApiParam(name = "id", value = "当前数据字典对象的id", required = true) @PathVariable Long id) {
		List<Dict> list = dictService.findChildData(id);
		return R.ok().data("list", list);
	}

	// 文件上传
	@ApiOperation(value = "数据字典导入")
	@PostMapping("importData")
	public R importData(@ApiParam(name = "file", value = "要导入的Excel文件，拿到文件输入流file.getInputStream()", required = true) MultipartFile file) {
		dictService.importDictData(file);
		return R.ok();
	}

	// 文件下载
	@ApiOperation(value = "数据字典导出")
	@GetMapping("exportData")
	public void exportData(@ApiParam(name = "httpServletResponse", value = "文件下载需要的文件输出流response.getOutputStream()", required = true) HttpServletResponse response) {
		dictService.exportDictData(response);
	}

	@ApiOperation(value = "根据value和数据字典码查询医院等级")
	@GetMapping("/getName/{value}/{dictCode}")
	public String getNameByValueAndDictCode(@ApiParam(name = "value", value = "数据字典中的value字段", required = true) @PathVariable String value,
											@ApiParam(name = "dictCode", value = "数据字典码") @PathVariable String dictCode) {
		return dictService.getName(value, dictCode);
	}

	@ApiOperation(value = "根据value查询医院所在省市区")
	@GetMapping("/getName/{value}")
	public String getNameByValueAndDictCode(@ApiParam(name = "value", value = "数据字典中的value字段", required = true) @PathVariable String value) {
		return dictService.getName(value, "");
	}

	@ApiOperation(value = "医院列表下拉框省市区联动，选择省之后根据字典码查询城省下的城市")
	@GetMapping("/findByDictCode/{dictCode}")
	public R findByDictCode(@ApiParam(name = "dictCode", value = "数据字典码", required = true) @PathVariable String dictCode) {
		List<Dict> list = dictService.findByDictCode(dictCode);
		return R.ok().data("list", list);
	}
}
