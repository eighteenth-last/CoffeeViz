package com.coffeeviz.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册成功事件
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final Long userId;
    private final String username;
    private final String email;

    public UserRegisteredEvent(Object source, Long userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
