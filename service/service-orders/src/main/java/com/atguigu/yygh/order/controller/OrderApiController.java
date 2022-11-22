package com.atguigu.yygh.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(value = "预约挂号接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {
	@Autowired
	private OrderService orderService;

	//代码中配置热点规则
	//构造方法
	public OrderApiController() {
		initRule();
	}

	// 平台端排班id（mg）
	@ApiOperation(value = "添加挂号订单")
	@PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
	public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
		Long orderId = orderService.saveOrder(scheduleId, patientId);
		return R.ok().data("orderId", orderId); // 返回给订单详情页面查询订单详情使用
	}

	// 订单详情页面调用
	@ApiOperation(value = "根据订单第查询订单详情")
	@GetMapping("auth/getOrder/{orderId}")
	public R getOrder(@PathVariable Long orderId) {
		OrderInfo orderInfo = orderService.getOrderInfo(orderId);
		return R.ok().data("orderInfo", orderInfo);
	}

	//订单列表
	@ApiOperation(value = "订单列表带参数的分页查询")
	@GetMapping("auth/{page}/{limit}")
	public R list(@PathVariable Long page
			, @PathVariable Long limit
			, OrderQueryVo orderQueryVo
			, HttpServletRequest request) {
		//service_utils  -->  JwtHelper
		//service_user ---> AuthContextHolder
		//注意：订单服务下不能添加用户服务的依赖
		// 或者使用AuthContextHolder也可以实现
		Long userId = JwtHelper.getUserId(request.getHeader("token"));
		// AuthContextHolder.getUserId(request)
		orderQueryVo.setUserId(userId);

		Page<OrderInfo> pageModel = new Page<>(page, limit);

		orderService.selectPage(pageModel, orderQueryVo);

		return R.ok().data("pageModel", pageModel);
	}

	//订单状态列表
	@ApiOperation(value = "订单列表状态查询")
	@GetMapping("auth/getStatusList")
	public R getStatusList() {
		List<Map<String, Object>> statusList = OrderStatusEnum.getStatusList();
		return R.ok().data("statusList", statusList);
	}

	@ApiOperation(value = "根据订单id查询订单，支付时使用")
	@GetMapping("getOrderInfoById/{orderId}")
	public OrderInfo getOrderInfoById(@PathVariable Long orderId) {
		return orderService.getById(orderId);
	}

	@ApiOperation(value = "更新订单信息，支付完成时时使用")
	@PutMapping("updateById")
	public void updateById(@RequestBody OrderInfo orderInfo) {
		orderService.updateById(orderInfo);
	}

	//取消预约
	@ApiOperation(value = "取消订单")
	@GetMapping("auth/cancelOrder/{orderId}")
	public R cancelOrder(@PathVariable("orderId") Long orderId) {
		Boolean flag = orderService.cancelOrder(orderId);
		return R.ok().data("flag", flag);
	}

	@ApiOperation(value = "数据统计")
	@PostMapping("inner/getCountMap")   //页面上的查询条件  医院名称 + 日期范围
	public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
		return orderService.getCountMap(orderCountQueryVo);
	}

	/**
	 * 导入热点值限流规则
	 * 也可在Sentinel dashboard界面配置（仅测试）
	 */
	public void initRule() {
		ParamFlowRule pRule = new ParamFlowRule("submitOrder")//资源名称，与SentinelResource值保持一致
				//限流第一个参数
				.setParamIdx(0)
				//单机阈值
				.setCount(10);
		// 针对 热点参数值单独设置限流 QPS 阈值，而不是全局的阈值.
		//如：1000（北京协和医院）,可以通过数据库表一次性导入，目前为测试
		ParamFlowItem item1 = new ParamFlowItem().setObject("10000")//热点值
				.setClassType(String.class.getName())//热点值类型
				.setCount(2);//热点值 QPS 阈值
		ParamFlowItem item2 = new ParamFlowItem().setObject("10001")//热点值
				.setClassType(String.class.getName())//热点值类型
				.setCount(5);//热点值 QPS 阈值
		List<ParamFlowItem> list = new ArrayList<>();
		list.add(item1);
		list.add(item2);

		pRule.setParamFlowItemList(list);
		ParamFlowRuleManager.loadRules(Collections.singletonList(pRule));
	}

	//测试接口--提交订单
	@PostMapping("auth/submitOrder/{hoscode}/{scheduleId}/{patientId}")
	//value：接口的资源名
	//blockHandler： 被限流时要执行的一个本地方法  （默认显示异常信息：Blocked by Sentinel (flow limiting)）
	@SentinelResource(value = "submitOrder", blockHandler = "submitOrderBlockHandler")
	public R submitOrder(@PathVariable String hoscode, @PathVariable String scheduleId, @PathVariable Long patientId) {
		Long orderId = 1L; //orderService.saveOrders(scheduleId,patientId);
		return R.ok().data("orderId", orderId);
	}

	//被限流时执行的方法
	public R submitOrderBlockHandler(String hoscode, String scheduleId, Long patientId, BlockException e) {
		return R.error().message("系统业务繁忙，请稍后下单");
	}
}
