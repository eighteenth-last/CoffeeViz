package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单 Mapper
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}
