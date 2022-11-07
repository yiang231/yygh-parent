package com.atguigu.yygh.cmn;

import com.alibaba.fastjson.JSON;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class CmnTest {
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	/*@Autowired
	private RedisTemplate<String, Object> redisTemplate1;*/
	@Autowired
	RedisTemplate<Object, Object> redisTemplate2;
	@Autowired
	private DictService dictService;
	@Autowired
	private RedisTemplate redisTemplate;

	@Test
	public void testFindChildData() {
		// 测试接口
		List<Dict> list = dictService.findChildData(1L);
		list.forEach(System.out::println);
	}

	@Test
	public void testRedis() {
		Set keys = redisTemplate.keys("*cache_*");
		//加了配置类可以查询出来[3232_ada_31_cache_dada_dada, dict_cache_1, cache_1]
		System.out.println(keys);
		//redisTemplate.delete(keys);
	}

	@Test
	public void test1() {
		//原生\xAC\xED\x00\x05t\x00\x05test2
		//添加配置类声明RedisTemplate<String,Object>，也有StringRedisTemplate一样的效果
		redisTemplate.boundValueOps("test2").set("hello");
		//stringRedisTemplate test2
		stringRedisTemplate.boundValueOps("test2").set("hello");
	}

	@Test
	public void testString() {
		//默认的序列化方式指定成string类型
		stringRedisTemplate.boundValueOps("atguigu").set("xian");
		Dict dict = new Dict();
		dict.setId(1L);
		dict.setName("hello");

		Dict dict2 = new Dict();
		dict2.setId(2L);
		dict2.setName("hello2");

		List<Dict> dictList = Arrays.asList(dict, dict2);
		stringRedisTemplate.boundValueOps("dictList").set(JSON.toJSONString(dictList));//list---> json字符串

		String aa = stringRedisTemplate.boundValueOps("dictList").get();
		List<Dict> dictList1 = JSON.parseArray(aa, Dict.class);
	}

	@Test
	public void test123() {
		//redis存取对象前后保持一致
		//redisTemplate1.boundValueOps("name").set("tom");
		redisTemplate2.boundValueOps("name").get();
	}
}
