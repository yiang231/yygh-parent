package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {
	@Autowired
	private DepartmentRepository departmentRepository;

	@Override
	public void save(Map<String, Object> parmaMap) {
		Department department = JSONObject.parseObject(JSONObject.toJSONString(parmaMap), Department.class);
		String depcode = department.getDepcode();
		String hoscode = department.getHoscode();
		Department department_mongo = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);

		if (department_mongo == null) {
			//新增
			department.setCreateTime(new Date());
		} else {
			//更新
			department.setId(department_mongo.getId());
		}
		department.setUpdateTime(new Date());
		departmentRepository.save(department);
	}

	@Override
	public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
		//分页mongo从1开始，倒序
		PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createTime"));
		//将DepartmentQueryVo中的数据拷贝到Department
		Department department = new Department();
		BeanUtils.copyProperties(departmentQueryVo, department);

		//模糊查询
		ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
		Example<Department> example = Example.of(department, exampleMatcher);

		Page<Department> pageResult = departmentRepository.findAll(example, pageable);

		return pageResult;
	}

	@Override
	public void remove(String hoscode, String depcode) {
		Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);

		if (department != null) {
			departmentRepository.deleteById(department.getId());
		}
	}
}
