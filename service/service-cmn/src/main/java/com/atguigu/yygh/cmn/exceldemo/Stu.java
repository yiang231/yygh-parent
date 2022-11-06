package com.atguigu.yygh.cmn.exceldemo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stu {
	@ExcelProperty(index = 0, value = "学生编号")
	private Integer id;
	@ExcelProperty(index = 1, value = "学生姓名")
	private String name;
}
