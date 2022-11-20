package com.atguigu.yygh.payment.service.impl;

import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.payment.mapper.RefundInfoMapper;
import com.atguigu.yygh.payment.service.RefundInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
}
