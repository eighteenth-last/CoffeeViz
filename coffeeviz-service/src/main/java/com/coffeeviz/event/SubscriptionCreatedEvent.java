package com.coffeeviz.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订阅成功事件
 */
@Getter
public class SubscriptionCreatedEvent extends ApplicationEvent {
    private final Long userId;
    private final String planName;
    private final String billingCycle;
    private final String endTime;

    public SubscriptionCreatedEvent(Object source, Long userId, String planName, String billingCycle, String endTime) {
        super(source);
        this.userId = userId;
        this.planName = planName;
        this.billingCycle = billingCycle;
        this.endTime = endTime;
    }
}
