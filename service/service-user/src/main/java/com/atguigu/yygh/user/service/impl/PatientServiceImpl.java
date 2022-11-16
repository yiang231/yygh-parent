package com.atguigu.yygh.user.service.impl;

import com.atguigu.cmn.client.DictFeignClient;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {
	@Autowired
	private DictFeignClient dictFeignClient;

	@Override
	public List<Patient> findListByUserId(Long userId) {
		QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id", userId);
		List<Patient> patientList = baseMapper.selectList(queryWrapper);
		//调用数据字典服务
		patientList.forEach(this::packPatient);
		return patientList;
	}

	@Override
	public Patient getPatientById(Long id) {
		Patient patient = baseMapper.selectById(id);
		this.packPatient(patient);
		return patient;
	}

	//给Param其他属性进行赋值
	private void packPatient(Patient patient) {
		String provinceCode = patient.getProvinceCode();
		String cityCode = patient.getCityCode();
		String districtCode = patient.getDistrictCode();

		String certificatesType = patient.getCertificatesType();//就诊人的证件类型
		String contactsCertificatesType = patient.getContactsCertificatesType();//联系人的证件类型

		String provinceString = dictFeignClient.getNameByValue(provinceCode);
		String cityString = dictFeignClient.getNameByValue(cityCode);
		String districtString = dictFeignClient.getNameByValue(districtCode);

		String certificatesTypeString = dictFeignClient.getNameByValue(certificatesType);
		String contactsCertificatesTypeString = dictFeignClient.getNameByValue(contactsCertificatesType);

		patient.getParam().put("provinceString", provinceString);
		patient.getParam().put("cityString", cityString);
		patient.getParam().put("districtString", districtString);
		patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
		patient.getParam().put("certificatesTypeString", certificatesTypeString);
		patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
	}
}
