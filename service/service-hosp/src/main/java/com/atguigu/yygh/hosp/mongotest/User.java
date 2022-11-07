package com.atguigu.yygh.hosp.mongotest;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

// 文档名称
@Data
@Document(collection = "user")//在数据库中的集合名
public class User {
	@Id
	private String id; // 24位_id
	private String name; //文档域 属性
	private Integer age;
	private String email;
	@JsonFormat(locale = "zh", timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;
}
