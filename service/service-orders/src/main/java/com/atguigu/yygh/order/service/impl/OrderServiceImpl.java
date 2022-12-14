package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.excp.YyghException;
import com.atguigu.yygh.common.utils.HttpRequestHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.mq.service.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.pay.client.PayFeignClient;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
	@Autowired
	private PatientFeignClient patientFeignClient;
	@Autowired
	private HospitalFeignClient hospitalFeignClient;
	@Autowired
	private RabbitService rabbitService;
	@Autowired
	private PayFeignClient payFeignClient;
	@Autowired
	private OrderInfoMapper orderInfoMapper;

	@Override
	public Long saveOrder(String scheduleId, Long patientId) {
		//-1、 当前就诊人在该排班下是否已经挂号
		//SELECT * FROM `order_info`
		//WHERE user_id = 53
		//AND patient_id = 6
		//AND hos_schedule_id = 109

		//查询排班（医院服务，mg）+查询就诊人（用户服务）
		//0、查询数据，为调用医院端接口做准备
		Patient patient = patientFeignClient.getPatientById(patientId);
		ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
		//1、调用医院端接口（创建订单，返回一些数据）
		// 根据医院编号查询该医院设置，取出api_url （远程服务调用）
		String api_url = hospitalFeignClient.getApiUrlByHoscode(scheduleOrderVo.getHoscode());
		String url = "http://" + api_url + "/order/submitOrder"; // 不同医院对应的端口

		// 封装数据
		Map<String, Object> parmaMap = new HashMap<>();
		parmaMap.put("hoscode", scheduleOrderVo.getHoscode());
		parmaMap.put("depcode", scheduleOrderVo.getDepcode());
		parmaMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
		parmaMap.put("reserveDate", new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
		parmaMap.put("reserveTime", scheduleOrderVo.getReserveTime());
		parmaMap.put("amount", scheduleOrderVo.getAmount()); //挂号费用
		parmaMap.put("name", patient.getName());
		parmaMap.put("certificatesType", patient.getCertificatesType());
		parmaMap.put("certificatesNo", patient.getCertificatesNo());
		parmaMap.put("sex", patient.getSex());
		parmaMap.put("birthdate", patient.getBirthdate());
		parmaMap.put("phone", patient.getPhone());
		parmaMap.put("isMarry", patient.getIsMarry());
		parmaMap.put("provinceCode", patient.getProvinceCode());
		parmaMap.put("cityCode", patient.getCityCode());
		parmaMap.put("districtCode", patient.getDistrictCode());
		parmaMap.put("address", patient.getAddress());
		parmaMap.put("contactsName", patient.getContactsName());
		parmaMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
		parmaMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
		parmaMap.put("contactsPhone", patient.getContactsPhone());
		parmaMap.put("timestamp", HttpRequestHelper.getTimestamp());
		parmaMap.put("sign", "");

		JSONObject jsonObject = HttpRequestHelper.sendRequest(parmaMap, url); // 可以看成map或者是JSON
		if (jsonObject.getInteger("code") == 200) {
			// 调用成功 医院接口返回三个值 code message data=[封装业务数据==还是json]
			// 根据接口文档从data中取值
			JSONObject data = jsonObject.getJSONObject("data");
			String hosRecordId = data.getString("hosRecordId");
			Integer number = data.getInteger("number");

			//从mg中查询到该排班，更新两个number
			Integer reservedNumber = data.getInteger("reservedNumber");
			Integer availableNumber = data.getInteger("availableNumber");

			String fetchTime = data.getString("fetchTime");
			String fetchAddress = data.getString("fetchAddress");

			//2、创建平台端自己的订单
			OrderInfo orderInfo = new OrderInfo();
			orderInfo.setUserId(patient.getUserId());

			String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
			orderInfo.setOutTradeNo(outTradeNo);//订单的编号（唯一） 付款，退款时都要使用
			orderInfo.setHoscode(scheduleOrderVo.getHoscode());
			orderInfo.setHosname(scheduleOrderVo.getHosname());
			orderInfo.setDepcode(scheduleOrderVo.getDepcode());
			orderInfo.setDepname(scheduleOrderVo.getDepname());
			orderInfo.setTitle(scheduleOrderVo.getTitle());
//            orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());//医院端排班的id
			orderInfo.setScheduleId(scheduleId);//mg中排班id
			orderInfo.setReserveDate(scheduleOrderVo.getReserveDate());
			orderInfo.setReserveTime(scheduleOrderVo.getReserveTime());
			orderInfo.setPatientId(patient.getId());
			orderInfo.setPatientName(patient.getName());
			orderInfo.setPatientPhone(patient.getPhone());

			orderInfo.setHosRecordId(hosRecordId);//当前平台端订单存储医院端订单的id

			orderInfo.setNumber(number);//取号序号
			orderInfo.setFetchTime(fetchTime);
			orderInfo.setFetchAddress(fetchAddress);
			orderInfo.setAmount(scheduleOrderVo.getAmount());
			orderInfo.setQuitTime(scheduleOrderVo.getQuitTime());
			orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());//0
			orderInfo.setCreateTime(new Date());
			orderInfo.setUpdateTime(new Date());

			baseMapper.insert(orderInfo);//`yygh_order`.`order_info`  平台端订单

			//3、更新mg中排班的号源数量 + 给用户（就诊人）发送短信通知  ----  mq-异步处理
			//向第一个队列发送消息
			//自定义消息对象
			this.afterSaveOrder(scheduleId, availableNumber, reservedNumber, patient);

			return orderInfo.getId();
		}
		return null;
	}

	// 根据订单第查询订单详情
	@Override
	public OrderInfo getOrderInfo(Long id) {
		OrderInfo orderInfo = baseMapper.selectById(id);
		this.packOrderInfo(orderInfo);
		return orderInfo;
	}

	//带条件参数的分页查询
	@Override
	public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
		QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
		if (orderQueryVo != null) {
			Long userId = orderQueryVo.getUserId();
			if (!StringUtils.isEmpty(userId)) {
				queryWrapper.eq("user_id", userId);
			}
			Long patientId = orderQueryVo.getPatientId();
			if (!StringUtils.isEmpty(patientId)) {
				queryWrapper.eq("patient_id", patientId);
			}
			String orderStatus = orderQueryVo.getOrderStatus();
			if (!StringUtils.isEmpty(orderStatus)) {
				queryWrapper.eq("order_status", orderStatus);
			}
		}
		this.page(pageParam, queryWrapper);

		// 订单状态进行转换
		pageParam.getRecords().forEach(this::packOrderInfo);
		return pageParam;
	}

	@Override
	public Boolean cancelOrder(Long orderId) {
		//1、判断当前订单是否允许被取消
		OrderInfo orderInfo = baseMapper.selectById(orderId);//平台端的订单
		Date quitTime = orderInfo.getQuitTime();
		DateTime dateTime = new DateTime(quitTime);
		if (dateTime.isBeforeNow()) {
			throw new YyghException(20001, "退号时间已过");
		}

		//2、调用医院端退号接口
		String apiUrl = hospitalFeignClient.getApiUrlByHoscode(orderInfo.getHoscode());
		String url = "http://" + apiUrl + "/order/updateCancelStatus";

		Map<String, Object> map = new HashMap<>();
		map.put("hoscode", orderInfo.getHoscode());
		map.put("hosRecordId", orderInfo.getHosRecordId());//医院端订单的id
//        map.put("timestamp",System.currentTimeMillis());
//        map.put("sign","");
		JSONObject jsonObject = HttpRequestHelper.sendRequest(map, url);

		if (jsonObject.getInteger("code") != 200) {
			//医院端退号接口调用失败
			throw new YyghException(20001, "医院端退号接口调用失败");
//            return false;
		}

//        Integer orderStatus = orderInfo.getOrderStatus();//1

		//3、平台端订单状态改成-1
		orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
		baseMapper.updateById(orderInfo);

		//4、判断是否需要退款
		//根据订单id和支付方式，获取支付记录
		PaymentInfo paymentInfo = payFeignClient.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
		if (paymentInfo.getPaymentStatus() == PaymentStatusEnum.PAID.getStatus()) {
			//已支付
			boolean bol = payFeignClient.isRefund(paymentInfo);//判断是否已经完成退款
			if (!bol) {
				//退款失败
				throw new YyghException(20001, "退款失败");
			}
		}
		//5、mq
		OrderMqVo orderMqVo = new OrderMqVo();
		orderMqVo.setScheduleId(orderInfo.getScheduleId());//mg中排班id
//        orderMqVo.setReservedNumber();
//        orderMqVo.setAvailableNumber();

		MsmVo msmVo = new MsmVo();
		msmVo.setPhone(orderInfo.getPatientPhone());//就诊人手机号
		msmVo.getParam().put("message", "订单取消成功");
		orderMqVo.setMsmVo(msmVo);

		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

		return true;
	}

	// 就医提醒
	@Override
	public void patientTips() {
		// 查询当天时间和就诊日期匹配的订单的集合
		QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
		String date = new DateTime().toString("yyyy-MM-dd");
		queryWrapper.eq("reserve_date", date);

		List<OrderInfo> list = this.list(queryWrapper);

		list.forEach(orderInfo -> {
			String patientName = orderInfo.getPatientName();
			String patientPhone = orderInfo.getPatientPhone();

			MsmVo msmVo = new MsmVo();
			msmVo.setPhone(patientPhone);
			msmVo.getParam().put("message", patientName + " 你好，" + "该吃药了");

			rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
		});
	}

	//数据统计
	@Override
	public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
		List<OrderCountVo> list = orderInfoMapper.selectOrderCount(orderCountQueryVo);
		// x--->list
		// y--->list

		// xlist + ylist ==> map

//        list.stream().collect();
		List<String> xList = new ArrayList<>();
		List<Integer> yList = new ArrayList<>();

//        list.forEach(orderCountVo -> {
//            xList.add(orderCountVo.getReserveDate());
//            yList.add(orderCountVo.getCount());
//        });
//
//        Map<String,Object> map = new HashMap<>();
//        map.put("dateList",xList);
//        map.put("countList",yList);

		//java8

		xList = list.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
		yList = list.stream().map(OrderCountVo::getCount).collect(Collectors.toList());

		Map<String, Object> map = new HashMap<>();
		map.put("dateList", xList);
		map.put("countList", yList);
		return map;
	}

	private void afterSaveOrder(String scheduleId, Integer availableNumber, Integer reservedNumber, Patient patient) {
		OrderMqVo orderMqVo = new OrderMqVo();
		orderMqVo.setScheduleId(scheduleId);//mongodb排班id
		orderMqVo.setAvailableNumber(availableNumber);
		orderMqVo.setReservedNumber(reservedNumber);

		MsmVo msmVo = new MsmVo();
		msmVo.setPhone(patient.getPhone());//就诊人手机号
//		msmVo.setTemplateCode("短信通知类型的短信模板");//根据山东鼎信客服申请
		msmVo.getParam().put("message", patient.getName() + "你好，订单创建成功");//短信内容
		orderMqVo.setMsmVo(msmVo);//医院服务向第二个对象发送的消息
		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
	}

	// 封装页面状态
	private void packOrderInfo(OrderInfo orderInfo) {
		Integer orderStatus = orderInfo.getOrderStatus();
		String statusNameByStatus = OrderStatusEnum.getStatusNameByStatus(orderStatus);
		orderInfo.getParam().put("orderStatusString", statusNameByStatus);
	}
}
