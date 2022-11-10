package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
	List<Dict> findChildData(Long id);

	void importDictData(MultipartFile file);

	void exportDictData(HttpServletResponse response);

	String getName(String value, String dictCode);

	List<Dict> findByDictCode(String dictCode);
}
