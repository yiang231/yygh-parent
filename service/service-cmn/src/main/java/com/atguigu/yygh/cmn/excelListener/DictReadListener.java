package com.atguigu.yygh.cmn.excelListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//逐行读取文档
@Component
public class DictReadListener extends AnalysisEventListener<DictEeVo> {
	@Autowired
	private DictMapper dictMapper;

	@Override
	public void invoke(DictEeVo dictEeVo, AnalysisContext context) {
		Dict dict = new Dict();
		//拷贝属性
		BeanUtils.copyProperties(dictEeVo, dict);
		dictMapper.insert(dict);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		System.out.println("所有行读取完执行");
	}
}
