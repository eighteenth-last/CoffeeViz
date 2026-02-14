package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffeeviz.common.util.Result;
import com.coffeeviz.entity.Notification;
import com.coffeeviz.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 客户端通知接口
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    /**
     * 获取当前用户的站内通知列表（分页）
     */
    @GetMapping
    public Result<?> getNotifications(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();

        Page<Notification> pageResult = notificationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Notification>()
                        .like(Notification::getChannels, "inbox")
                        .and(w -> w.eq(Notification::getTarget, "all")
                                .or().eq(Notification::getTarget, String.valueOf(userId)))
                        .eq(Notification::getStatus, "sent")
                        .orderByDesc(Notification::getCreateTime));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Notification n : pageResult.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId());
            item.put("title", n.getTitle());
            item.put("content", n.getContent());
            item.put("time", n.getCreateTime());
            item.put("read", n.getIsRead() != null && n.getIsRead() == 1);
            list.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", list);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();

        long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .like(Notification::getChannels, "inbox")
                        .and(w -> w.eq(Notification::getTarget, "all")
                                .or().eq(Notification::getTarget, String.valueOf(userId)))
                        .eq(Notification::getStatus, "sent")
                        .and(w -> w.isNull(Notification::getIsRead)
                                .or().eq(Notification::getIsRead, 0)));

        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable(value = "id") Long id) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, id)
                        .set(Notification::getIsRead, 1));
        return Result.success("已标记为已读");
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/read-all")
    public Result<String> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();

        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .like(Notification::getChannels, "inbox")
                        .and(w -> w.eq(Notification::getTarget, "all")
                                .or().eq(Notification::getTarget, String.valueOf(userId)))
                        .eq(Notification::getStatus, "sent")
                        .and(w -> w.isNull(Notification::getIsRead)
                                .or().eq(Notification::getIsRead, 0))
                        .set(Notification::getIsRead, 1));

        return Result.success("全部已读");
    }
}
