package com.atguigu.yygh.cmn.exceldemo;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

//逐行读取文档
public class StuReadListener extends AnalysisEventListener<Stu> {

	@Override
	public void invoke(Stu data, AnalysisContext context) {
		System.out.println("读取数据");
		System.out.println("data = " + data);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		System.out.println("所有行读取完执行");
	}
}
