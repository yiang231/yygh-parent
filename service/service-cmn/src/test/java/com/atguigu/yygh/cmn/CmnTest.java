package com.atguigu.yygh.cmn;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CmnTest {
	@Autowired
	private DictService dictService;

	@Test
	public void testFindChildData() {
		// 测试接口
		List<Dict> list = dictService.findChildData(1L);
		list.forEach(System.out::println);
	}
}
