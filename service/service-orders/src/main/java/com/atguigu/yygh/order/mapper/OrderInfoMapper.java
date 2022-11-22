package com.atguigu.yygh.order.mapper;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
	List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);
}
