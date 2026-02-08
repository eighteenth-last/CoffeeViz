package com.coffeeviz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.PaymentOrder;
import com.coffeeviz.entity.SubscriptionPlan;
import com.coffeeviz.entity.UserSubscription;
import com.coffeeviz.mapper.PaymentOrderMapper;
import com.coffeeviz.mapper.SubscriptionPlanMapper;
import com.coffeeviz.service.PaymentOrderService;
import com.coffeeviz.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 支付订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderServiceImpl implements PaymentOrderService {
    
    private final PaymentOrderMapper orderMapper;
    private final SubscriptionPlanMapper planMapper;
    private final SubscriptionService subscriptionService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrder createOrder(Long userId, Long planId, String billingCycle, String paymentMethod) {
        log.info("创建支付订单: userId={}, planId={}, cycle={}", userId, planId, billingCycle);
        
        SubscriptionPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("订阅计划不存在");
        }
        
        BigDecimal amount = "yearly".equals(billingCycle) 
            ? plan.getPriceYearly() 
            : plan.getPriceMonthly();
        
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPlanId(planId);
        order.setPlanCode(plan.getPlanCode());
        order.setBillingCycle(billingCycle);
        order.setAmount(amount);
        order.setCurrency("CNY");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("pending");
        
        orderMapper.insert(order);
        return order;
    }
    
    @Override
    public PaymentOrder getByOrderNo(String orderNo) {
        return orderMapper.selectOne(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOrderNo, orderNo)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(String orderNo, String status, String transactionId) {
        PaymentOrder order = getByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        
        order.setPaymentStatus(status);
        order.setTransactionId(transactionId);
        
        if ("paid".equals(status)) {
            order.setPaymentTime(LocalDateTime.now());
        }
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(String orderNo, String transactionId) {
        log.info("处理支付成功: orderNo={}, transactionId={}", orderNo, transactionId);
        
        PaymentOrder order = getByOrderNo(orderNo);
        if (order == null) {
            log.error("订单不存在: {}", orderNo);
            return false;
        }
        
        if ("paid".equals(order.getPaymentStatus())) {
            log.warn("订单已支付: {}", orderNo);
            return true;
        }
        
        // 更新订单状态
        order.setPaymentStatus("paid");
        order.setTransactionId(transactionId);
        order.setPaymentTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 创建或升级订阅
        UserSubscription subscription = subscriptionService.createSubscription(
            order.getUserId(), 
            order.getPlanId(), 
            order.getBillingCycle()
        );
        
        // 关联订阅ID
        order.setSubscriptionId(subscription.getId());
        orderMapper.updateById(order);
        
        log.info("支付成功处理完成: orderNo={}", orderNo);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundOrder(String orderNo, String reason) {
        log.info("退款订单: orderNo={}, reason={}", orderNo, reason);
        
        PaymentOrder order = getByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        
        order.setPaymentStatus("refunded");
        order.setRefundTime(LocalDateTime.now());
        order.setRefundReason(reason);
        
        // 取消关联的订阅
        if (order.getSubscriptionId() != null) {
            subscriptionService.cancelSubscription(order.getUserId(), "订单退款");
        }
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    public List<PaymentOrder> getUserOrders(Long userId) {
        return orderMapper.selectList(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getUserId, userId)
                .orderByDesc(PaymentOrder::getCreateTime)
        );
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "PAY" + timestamp + random;
    }
}
