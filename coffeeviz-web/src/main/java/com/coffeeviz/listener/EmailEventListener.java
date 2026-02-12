package com.coffeeviz.listener;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.EmailTemplate;
import com.coffeeviz.entity.User;
import com.coffeeviz.event.SubscriptionCreatedEvent;
import com.coffeeviz.event.UserRegisteredEvent;
import com.coffeeviz.mapper.EmailTemplateMapper;
import com.coffeeviz.mapper.UserMapper;
import com.coffeeviz.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 邮件事件监听器 - 监听注册/订阅事件自动发送邮件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;
    private final EmailTemplateMapper templateMapper;
    private final UserMapper userMapper;

    /**
     * 用户注册成功 → 发送欢迎邮件（事务提交后触发）
     */
    @Async
    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.getEmail() == null || event.getEmail().isBlank()) {
            log.info("用户未填写邮箱，跳过欢迎邮件: userId={}", event.getUserId());
            return;
        }
        try {
            JSONObject config = emailService.getEmailConfig();
            if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
                log.debug("邮件服务未启用，跳过欢迎邮件");
                return;
            }

            EmailTemplate template = templateMapper.selectOne(
                    new LambdaQueryWrapper<EmailTemplate>()
                            .eq(EmailTemplate::getTemplateCode, "welcome")
                            .eq(EmailTemplate::getStatus, 1));
            if (template == null) {
                log.debug("未找到启用的欢迎邮件模板(welcome)，跳过");
                return;
            }

            String username = event.getUsername();
            String subject = replaceVars(template.getSubject(), username, event.getEmail(), null, null, null);
            String content = replaceVars(template.getContent(), username, event.getEmail(), null, null, null);

            emailService.sendSingleEmail(event.getEmail(), subject, content, "welcome");
            log.info("欢迎邮件已发送: userId={}, email={}", event.getUserId(), event.getEmail());
        } catch (Exception e) {
            log.error("发送欢迎邮件失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 订阅成功 → 发送订阅确认邮件（事务提交后触发）
     */
    @Async
    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionCreated(SubscriptionCreatedEvent event) {
        try {
            JSONObject config = emailService.getEmailConfig();
            if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
                log.debug("邮件服务未启用，跳过订阅邮件");
                return;
            }

            User user = userMapper.selectById(event.getUserId());
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.info("用户无邮箱，跳过订阅邮件: userId={}", event.getUserId());
                return;
            }

            EmailTemplate template = templateMapper.selectOne(
                    new LambdaQueryWrapper<EmailTemplate>()
                            .eq(EmailTemplate::getTemplateCode, "subscription_success")
                            .eq(EmailTemplate::getStatus, 1));
            if (template == null) {
                log.debug("未找到启用的订阅邮件模板(subscription_success)，跳过");
                return;
            }

            String username = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
            String subject = replaceVars(template.getSubject(), username, user.getEmail(),
                    event.getPlanName(), event.getBillingCycle(), event.getEndTime());
            String content = replaceVars(template.getContent(), username, user.getEmail(),
                    event.getPlanName(), event.getBillingCycle(), event.getEndTime());

            emailService.sendSingleEmail(user.getEmail(), subject, content, "subscription_success");
            log.info("订阅邮件已发送: userId={}, plan={}", event.getUserId(), event.getPlanName());
        } catch (Exception e) {
            log.error("发送订阅邮件失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 模板变量替换
     */
    private String replaceVars(String text, String username, String email,
                               String planName, String billingCycle, String endTime) {
        if (text == null) return "";
        String result = text
                .replace("{{username}}", username != null ? username : "")
                .replace("{{email}}", email != null ? email : "");
        if (planName != null) result = result.replace("{{planName}}", planName);
        if (billingCycle != null) {
            String cycleText = "yearly".equals(billingCycle) ? "年付" : "月付";
            result = result.replace("{{billingCycle}}", cycleText);
        }
        if (endTime != null) {
            // 格式化: 2026-03-12T16:39:13 → 2026-03-12
            String dateOnly = endTime.length() > 10 ? endTime.substring(0, 10) : endTime;
            result = result.replace("{{endTime}}", dateOnly);
        }
        return result;
    }
}
