package com.atguigu.yygh.oss.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/oss/file")
public class FileUploadController {
	@Autowired
	FileService fileService;

	@PostMapping("upload")
	public R upload(@RequestParam("file") MultipartFile file) {
		String url = fileService.upload(file);//上传到oss后返回的文件的地址
		return R.ok().data("url", url);
	}
}
