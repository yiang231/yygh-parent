package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.mongotest.User;
import com.atguigu.yygh.hosp.mongotest.UserRepository;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@SpringBootTest
public class MongoTest2 {

	@Autowired
	UserRepository userRepository;
	@Autowired
	HospitalSetService hospitalSetService;

	@Test
	public void test1() {
//        userRepository.save(); //添加（根据id修改）一个文档
//        userRepository.saveAll(); //批量添加
//
//        userRepository.insert(); //只是添加、如果id存在了，会报错
//
//        userRepository.findAll();
//        userRepository.findById();
//        userRepository.deleteAll();
//        userRepository.deleteById();
	}

	@Test
	public void test2() {
//        userRepository.findAll(Sort.by(Sort.Direction.ASC,"age"));
//        userRepository.findAll(PageRequest.of(0,5));
//
//        User user = new User();
//        user.setAge(10);
//        user.setName("tom");
//        Example<User> example = Example.of(user);// where age = ? and name = ?
//        List<User> all = userRepository.findAll(example);
	}

	//模糊查询
	@Test
	public void test3() {
		//name like ?
		User user = new User();
		user.setName("tom");

		//条件匹配器---模糊查询匹配
		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
				.withIgnoreCase(true) //忽略大小写
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);// 字符串包含

		Example<User> example = Example.of(user, exampleMatcher);// name = ?
		List<User> all = userRepository.findAll(example);
		System.out.println(all);
	}

	//带分页排序的模糊查询
	@Test
	public void test4() {
		//name模糊查询，分页，排序
		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "age"));

		User user = new User();
		user.setName("tom");
		//条件匹配器---模糊查询匹配
		ExampleMatcher matcher = ExampleMatcher.matching()
				.withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<User> example = Example.of(user, matcher);
		//org.springframework.data.domain
		Page<User> page = userRepository.findAll(example, pageable);
		List<User> content = page.getContent();//当前页数据
		long totalElements = page.getTotalElements();//总记录数
	}

	@Test
	public void testPage() {
//		Page<HospitalSet> hospitalSets = new Page<>(1, 5);//两个page，必须有一个写全限定类名
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<HospitalSet> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 5);
		hospitalSetService.page(page);

		List<HospitalSet> records = page.getRecords();
		long total = page.getTotal();
	}
}
