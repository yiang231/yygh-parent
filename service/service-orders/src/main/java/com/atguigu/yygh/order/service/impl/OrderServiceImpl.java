package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
	@Autowired
	private PatientFeignClient patientFeignClient;
	@Autowired
	private HospitalFeignClient hospitalFeignClient;

	@Override
	public Long saveOrder(String scheduleId, Long patientId) {
		//查询排班（医院服务，mg）+查询就诊人（用户服务）
		//0、查询数据，为调用医院端接口做准备
		Patient patient = patientFeignClient.getPatientById(patientId);
		ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
		//1、调用医院端接口（创建订单，返回一些数据）


		//2、创建平台端自己的订单


		//3、更新mg中排班的号源数量 + 给用户发送短信通知
		return null;
	}
}
