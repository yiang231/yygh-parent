package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	// 查询所有的科室信息便于排版页面左侧展示
	@Override
	public List<DepartmentVo> findDeptTree(String hoscode) {
		//0、创建集合用于数据封装
		List<DepartmentVo> departmentVoList = new ArrayList<>(); // 大科室集合
		//1、根据医院编号查询所有的小科室
		List<Department> departmentList = departmentRepository.findByHoscode(hoscode);
		//2、根据 bigcode 进行分组，使用 collect
		Map<String, List<Department>> collect = departmentList.stream()
				.collect(Collectors.groupingBy(Department::getBigcode));
		//3、对 map 进行遍历，得到大科室对象
		collect.forEach((key, value) -> {
			//key = bigcode , value = bigcode 相同的小科室集合
			DepartmentVo departmentVo = new DepartmentVo();
			departmentVo.setDepcode(key);//大科室编号
			departmentVo.setDepname(value.get(0).getBigname());// 大科室名称
			departmentVo.setChildren(this.transferDepartmentVo(value));

			departmentVoList.add(departmentVo);
		});
		return departmentVoList;
	}

	@Override
	public Department findDepartment(String hoscode, String depcode) {
		return departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
	}

	// 对象转换
	public List<DepartmentVo> transferDepartmentVo(List<Department> list) {
		//department 转 departmentVo
		List<DepartmentVo> departmentVoList = new ArrayList<>();

		list.forEach(department -> {
			DepartmentVo departmentVo = new DepartmentVo();

			departmentVo.setDepcode(department.getDepcode());
			departmentVo.setDepname(department.getDepname());
			departmentVo.setChildren(null);

			departmentVoList.add(departmentVo);
		});
		return departmentVoList;
	}
}
