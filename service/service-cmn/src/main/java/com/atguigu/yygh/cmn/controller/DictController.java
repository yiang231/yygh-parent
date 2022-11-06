package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(description = "数据字典接口")//加在类上
@CrossOrigin//设置跨域请求
@RestController
@RequestMapping("/admin/cmn/dict")
public class DictController {
	@Autowired
	public DictService dictService;

	@ApiOperation(value = "根据数据id查询子数据列表")
	@GetMapping("findChildData/{id}")
	public Result findChildData(@ApiParam(name = "id", value = "当前数据字典对象的id", required = true) @PathVariable Long id) {
		List<Dict> list = dictService.findChildData(id);
		return Result.ok().data("list", list);
	}

	// 文件上传
	@ApiOperation(value = "数据字典导入")
	@PostMapping("importData")
	public Result importData(@ApiParam(name = "file", value = "要导入的Excel文件，拿到文件输入流file.getInputStream()", required = true) MultipartFile file) {
		dictService.importDictData(file);
		return Result.ok();
	}

	// 文件下载
	@ApiOperation(value = "数据字典导出")
	@GetMapping("exportData")
	public void exportData(@ApiParam(name = "httpServletResponse", value = "文件下载需要的文件输出流response.getOutputStream()", required = true) HttpServletResponse response) {
		dictService.exportDictData(response);
	}
}
