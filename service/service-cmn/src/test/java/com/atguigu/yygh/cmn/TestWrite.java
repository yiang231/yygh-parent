package com.atguigu.yygh.cmn;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.exceldemo.Stu;
import com.atguigu.yygh.cmn.exceldemo.StuReadListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestWrite {
	@Test
	public void excel() {
		Stu stu1 = new Stu(12, "张三");
		Stu stu2 = new Stu(12, "张三");
		Stu stu3 = new Stu(12, "张三");

		List<Stu> list = Arrays.asList(stu1, stu2, stu3);
		//本地文件写入
		EasyExcel.write("C:\\Users\\Yiang\\Desktop\\学生列表.xls", Stu.class).sheet("学生列表").doWrite(list);
		//读取文件信息
		EasyExcel.read("C:\\Users\\Yiang\\Desktop\\学生列表.xls", Stu.class, new StuReadListener()).sheet("学生列表").doRead();
	}

}
