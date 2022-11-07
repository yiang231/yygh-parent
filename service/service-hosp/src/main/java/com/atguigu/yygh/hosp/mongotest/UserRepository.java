package com.atguigu.yygh.hosp.mongotest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	//查询 方法  get find  select
	// age = ？
	List<User> findByAge(Integer age);

	//age > ? and name like  ?
	List<User> findByAgeGreaterThanAndNameLike(Integer age, String name);

	User findByNameLike(String name);//如果该方法实际查询到多个文档对象，就会报错。该方法最多只能查询到一个文档

	// 删除
	void removeByNameLike(String name);

	//排序
	List<User> findByAgeOrNameLikeOrderByAge(Integer age, String name);// age = ? or name like ?
}
