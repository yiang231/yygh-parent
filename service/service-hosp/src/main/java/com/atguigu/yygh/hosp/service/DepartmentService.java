package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
	void save(Map<String, Object> parmaMap);

	Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo);

	void remove(String hoscode, String depcode);

	List<DepartmentVo> findDeptTree(String hoscode);

	Department findDepartment(String hoscode, String depcode);
}
