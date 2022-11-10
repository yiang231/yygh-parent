package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.cmn.client.DictFeignClient;
import com.atguigu.yygh.common.excp.YyghException;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private HospitalRepository hospitalRepository;
	@Autowired
	private HospitalSetService hospitalSetService;
	@Autowired
	private DictFeignClient dictFeignClient;

	@Override
	public void save(Map<String, Object> paramMap) {
		//0、签名校验【前提】
		String signKey = (String) paramMap.get("sign");//验签参数已经加密
		if (StringUtils.isEmpty(signKey)) {
			throw new YyghException(20001, "签名不存在");
		}
		//拿到唯一标识hoscode
		String hoscode = (String) paramMap.get("hoscode");
		if (StringUtils.isEmpty(hoscode)) {
			throw new YyghException(20001, "医院编号不能为空");
		}
		//从数据库查询signKey
		HospitalSet hospitalSet = hospitalSetService.findByHoscode(hoscode);
		if (StringUtils.isEmpty(hospitalSet)) {
			throw new YyghException(20001, "该医院暂未开通权限");
		}
		String signKeyRaw = hospitalSet.getSignKey();//未加密
		String encrypt = MD5.encrypt(signKeyRaw);
		if (!encrypt.equals(signKey)) {
			throw new YyghException(20001, "签名校验失败");
		}

		//1、使用JSON将Map转化为Hospital对象
		String toJSONString = JSONObject.toJSONString(paramMap);
//		JSONObject.parseObject();  //字符串={}
//		JSONObject.parseArray();   //字符串=[{},{},{}]
		Hospital hospital = JSONObject.parseObject(toJSONString, Hospital.class);//医院端传来的值

		//将logoData进行base64转化，空格变成加号
		String logoData = hospital.getLogoData();
		String logoDataReplace = logoData.replaceAll(" ", "+");
		hospital.setLogoData(logoDataReplace);

		//logoData进行转化
		//2、根据hoscode从mongodb中查询数据是否存在医院设置
		/*//方法一
		Query query = new Query();
		query.addCriteria(Criteria.where("hoscode").is(hoscode));
		mongoTemplate.findOne(query, Hospital.class);*/

		/*//方法二
		Hospital hospital2 = new Hospital();
		hospital2.setHoscode(hoscode);
		Example<Hospital> example = Example.of(hospital2);//T代表需要实体类
//		Optional<Hospital> one = hospitalRepository.findOne(example); //调用get方法拿到想要的对象
		Hospital hospitalFindOne = hospitalRepository.findOne(example).get();*/

		//方法三 HospitalRepository自定义方法
		Hospital hospital_mongo = hospitalRepository.findByHoscode(hoscode);

		//3、新增或者更新
		if (hospital_mongo == null) {
			//无数据新增数据
			hospital.setCreateTime(new Date());
			hospital.setStatus(1);//开通并且上线
		} else {
			//有数据更新数据
			hospital.setId(hospital_mongo.getId());//根据id更新
		}
		hospital.setUpdateTime(new Date());
		hospitalRepository.save(hospital);
	}

	@Override
	public Hospital getByHoscode(Map<String, Object> paramMap) {
		String hoscode = (String) paramMap.get("hoscode");
		if (StringUtils.isEmpty(hoscode)) {
			throw new YyghException(20001, "医院编号不能为空");
		}
		return hospitalRepository.findByHoscode(hoscode);
	}

	@Override
	public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
		Hospital hospital = new Hospital();
		BeanUtils.copyProperties(hospitalQueryVo, hospital);

		PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createTime"));

		ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
		Example<Hospital> example = Example.of(hospital, exampleMatcher);
		Page<Hospital> pageResult = hospitalRepository.findAll(example, pageable);

		//医院服务调用数据字典服务
		pageResult.getContent().forEach(this::packHospital);
		return pageResult;
	}

	@Override
	public void updateStatus(String id, Integer status) {
		Hospital hospital = hospitalRepository.findById(id).get();
		hospital.setStatus(status);
		hospitalRepository.save(hospital);
	}

	@Override
	public Hospital show(String id) {
		Hospital hospital = hospitalRepository.findById(id).get();
		this.packHospital(hospital);
		return hospital;
	}

	private void packHospital(Hospital hosp) {
		//这四条属性在mongo中对应数据字典value的值
		String hostype = hosp.getHostype();
		String provinceCode = hosp.getProvinceCode();
		String cityCode = hosp.getCityCode();
		String districtCode = hosp.getDistrictCode();

		String hosTypeString = dictFeignClient.getNameByValueAndDictCode(hostype, DictEnum.HOSTYPE.getDictCode());
		String provinceString = dictFeignClient.getNameByValueAndDictCode(provinceCode);
		String cityString = dictFeignClient.getNameByValueAndDictCode(cityCode);
		String districtString = dictFeignClient.getNameByValueAndDictCode(districtCode);

		String fullAddress = provinceString + cityString + districtString + hosp.getAddress();

		//其他参数信息
		hosp.getParam().put("hosTypeString", hosTypeString);
		hosp.getParam().put("fullAddress", fullAddress);
	}
}
