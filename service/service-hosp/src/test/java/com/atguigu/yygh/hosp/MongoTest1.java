package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.mongotest.User;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@SpringBootTest
public class MongoTest1 {

	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	HospitalSetService hospitalSetService;

	@Test
	public void test1() {
		User user = new User();
//        user.setId(); //不需要,mg自动生成
		user.setName("tom122");
		user.setAge(20);
		user.setEmail("test");
		user.setCreateDate(new Date());

//        mongoTemplate.insertAll(); //批量添加--参数collection集合--list
		mongoTemplate.insert(user);
//        mongoTemplate.insert(user,"user"); // 如果类上没有标注集合的名字，调用insert方法的时候自己指定
	}

	@Test
	public void test2() {
		//查询所有
		List<User> all = mongoTemplate.findAll(User.class);
		System.out.println(all);
	}

	@Test
	public void test3() {
		User byId = mongoTemplate.findById("6368e13acf77d12ad496f712", User.class);
		System.out.println(byId);
	}

	@Test
	public void test4() {
//		条件查询  age = 20
//		写法1：
//        Query query = new Query();
//        query.addCriteria(Criteria.where("age").is(20));
//		写法：  where age  = 20 and name = 'tom'
		Query query = new Query(Criteria.where("age").is(20).and("name").is("tom"));
		List<User> users = mongoTemplate.find(query, User.class);
		System.out.println(users);
	}

	//模糊查询（了解）
	@Test
	public void test5() {
		String name = "张";//  where name like '%张%'

		Query query = new Query();

		//模糊查询匹配格式
		String regex = String.format("%s%s%s", "^.*", name, ".*$");
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

//        query.addCriteria(Criteria.where("name").is(name));//name = ？
		query.addCriteria(Criteria.where("name").regex(name).and("age").is(21));//name like ？ 将is换成regex

		List<User> users = mongoTemplate.find(query, User.class);
		users.forEach(System.out::println);
	}

	//分页查询
	@Test
	public void test6() {
		long page = 1;
		int limit = 5;
		Query query = new Query(Criteria.where("age").is(20));//where age = 20
		//skip = 跳过多少条数据，limit = 每页条数
		query.skip((page - 1) * limit).limit(limit);
		List<User> list = mongoTemplate.find(query, User.class);
		list.forEach(System.out::println);
	}

	//修改数据
	@Test
	public void test7() {
//        mongoTemplate.updateFirst();  //相当于updateOne
		Query query = new Query(Criteria.where("age").is(20));

		Update update = new Update();
		update.set("email", "atguigu@you");
		update.set("age", 21);

		UpdateResult updateResult = mongoTemplate.updateMulti(query, update, User.class);
		long modifiedCount = updateResult.getModifiedCount();
		System.out.println("modifiedCount = " + modifiedCount);
	}

	//删除数据
	@Test
	public void test8() {
//        mongoTemplate.remove(User.class);//删除所有
		Query query = new Query(Criteria.where("age").is(21));
		mongoTemplate.remove(query, User.class);
	}

	@Test
	public void testAll() {
//        mongoTemplate.save(); //添加（修改）一个文档,如果user对象中的id在数据库中如果存在，则执行的是根据id进行修改
//        mongoTemplate.insert(); //添加一个文档，这个user对象的id值在数据库如果存在，出现报错，E11000 duplicate key error collection: yygh.user index: _id_ dup key
//        mongoTemplate.insertAll();// 批量添加
//
//        new QueryWrapper<>();//mp-mysql
		String regex = String.format("%s%s%s", "^.*", "张", ".*$");
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Query query = new Query(Criteria.where("age").is(20).and("name").regex(pattern));
		Update update = new Update();
		update.set("name", "新的名字");
		mongoTemplate.updateMulti(query, update, User.class);

//        User user = new User();
//        user.setId("6368b09d904b0b0cf8156045");
//        user.setName("jack");
//        user.setAge(20);
//        user.setEmail("test");
//        user.setCreateDate(new Date());
//        mongoTemplate.save(user);

//        mongoTemplate.findById()
//        mongoTemplate.findAll()
//        mongoTemplate.find()
	}

	//或条件分页查询
	@Test
	public void testOr() {
		Query query = new Query(Criteria.where("name").is("小明").orOperator(Criteria.where("age").gt(20), Criteria.where("age").lt(33)));
		query.with(Sort.by(Sort.Direction.ASC, "age", "name"));
		//Pageable
		Pageable pageable = PageRequest.of(1, 5);
		query.with(pageable);//分页
	}

	//新增或添加
	@Test
	public void testUpsert() {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is("test123"));
		Update update = new Update();
		update.set("name", "张三");
		mongoTemplate.upsert(query, update, User.class);
	}

	@Test
	public void test() {
		Query query = new Query();
		Criteria criteria1 = Criteria.where("name").is("tom");
		Criteria criteria2 = Criteria.where("age").is(20);
		//注意：orOperator方法的参数是一个可变形参列表，所以该方法只调用一次即可
		query.addCriteria(new Criteria().orOperator(criteria1, criteria2));//where name = ？ or  age = ？ ；
		Pageable pageable = PageRequest.of(0, 6);//第一页
		query.with(pageable);
	}

	@Test
	public void testMp() {
		IPage<HospitalSet> page = new Page<>(1, 5);//1==第一页
		hospitalSetService.page(page, null);
	}
}
