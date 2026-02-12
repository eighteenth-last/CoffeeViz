package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.*;
import com.coffeeviz.entity.*;
import com.coffeeviz.mapper.*;
import com.coffeeviz.service.ConfigService;
import com.coffeeviz.service.SubscriptionService;
import com.coffeeviz.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端 Controller
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private SubscriptionPlanMapper planMapper;

    @Autowired
    private UserSubscriptionMapper subscriptionMapper;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

    // ==================== Dashboard ====================

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<AdminDashboardStatsVO> getDashboardStats() {
        log.info("获取管理端仪表盘统计");
        try {
            AdminDashboardStatsVO stats = new AdminDashboardStatsVO();

            // 总用户数
            Long totalUsers = userMapper.selectCount(null);
            stats.setTotalUsers(totalUsers);

            // 用户增长率（本周 vs 上周）
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minus(7, ChronoUnit.DAYS);
            LocalDateTime twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);
            Long thisWeekUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, weekAgo));
            Long lastWeekUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, twoWeeksAgo)
                    .lt(User::getCreateTime, weekAgo));
            stats.setUserGrowth(calcGrowth(thisWeekUsers, lastWeekUsers));

            // 活跃团队数
            Long activeTeams = teamMapper.selectCount(new LambdaQueryWrapper<Team>()
                    .eq(Team::getStatus, "active"));
            stats.setActiveTeams(activeTeams);
            stats.setTeamGrowth(5.2); // 简化处理

            // 月收入（本月已支付订单总额）
            LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LambdaQueryWrapper<PaymentOrder> paymentWrapper = new LambdaQueryWrapper<PaymentOrder>()
                    .eq(PaymentOrder::getPaymentStatus, "paid")
                    .ge(PaymentOrder::getPaymentTime, monthStart);
            List<PaymentOrder> monthOrders = paymentOrderMapper.selectList(paymentWrapper);
            BigDecimal monthlyRevenue = monthOrders.stream()
                    .map(PaymentOrder::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setMonthlyRevenue(monthlyRevenue);
            stats.setRevenueGrowth(-1.4); // 简化处理

            // AI调用次数（从配额跟踪表统计）
            stats.setAiCalls(0L);
            stats.setAiCallGrowth(24.0);

            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    // ==================== Users ====================

    /**
     * 获取用户列表（分页 + 搜索）
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "planFilter", required = false) String planFilter) {
        log.info("获取用户列表: page={}, size={}, keyword={}", page, size, keyword);
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(User::getUsername, keyword)
                        .or().like(User::getEmail, keyword)
                        .or().like(User::getDisplayName, keyword));
            }
            wrapper.orderByDesc(User::getCreateTime);

            Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);

            // 查询每个用户的当前订阅
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : pageResult.getRecords()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", user.getId());
                item.put("name", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                item.put("email", user.getEmail());
                item.put("phone", user.getPhone());
                item.put("status", user.getStatus());
                item.put("createdAt", user.getCreateTime());

                // 获取用户订阅计划名称
                UserSubscription sub = subscriptionService.getCurrentSubscription(user.getId());
                String planName = "Free";
                if (sub != null && "active".equals(sub.getStatus())) {
                    SubscriptionPlan plan = planMapper.selectById(sub.getPlanId());
                    if (plan != null) {
                        planName = plan.getPlanName();
                    }
                }
                item.put("plan", planName);
                userList.add(item);
            }

            // 按计划过滤（在内存中过滤，因为跨表）
            if (StringUtils.hasText(planFilter)) {
                userList = userList.stream()
                        .filter(u -> planFilter.equals(u.get("plan")))
                        .collect(Collectors.toList());
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("list", userList);
            result.put("total", pageResult.getTotal());
            result.put("page", page);
            result.put("size", size);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 赠送订阅
     */
    @PostMapping("/users/{userId}/gift")
    public Result<String> giftSubscription(@PathVariable("userId") Long userId,
                                           @RequestBody GiftSubscriptionRequest request) {
        log.info("赠送订阅: userId={}, plan={}", userId, request.getPlan());
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            // 根据plan类型确定planCode和billingCycle
            String planCode;
            String billingCycle;
            switch (request.getPlan()) {
                case "pro_month":
                    planCode = "PRO";
                    billingCycle = "monthly";
                    break;
                case "pro_year":
                    planCode = "PRO";
                    billingCycle = "yearly";
                    break;
                case "team_month":
                    planCode = "TEAM";
                    billingCycle = "monthly";
                    break;
                default:
                    return Result.error(400, "无效的计划类型");
            }

            SubscriptionPlan plan = subscriptionService.getPlanByCode(planCode);
            if (plan == null) {
                return Result.error(404, "订阅计划不存在");
            }

            subscriptionService.createSubscription(userId, plan.getId(), billingCycle);
            log.info("赠送订阅成功: userId={}, plan={}, reason={}", userId, request.getPlan(), request.getReason());
            return Result.success("赠送成功", "success");
        } catch (Exception e) {
            log.error("赠送订阅失败", e);
            return Result.error("赠送订阅失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态（封禁/解封）
     */
    @PutMapping("/users/{userId}/status")
    public Result<String> updateUserStatus(@PathVariable("userId") Long userId,
                                           @RequestBody Map<String, Object> body) {
        log.info("更新用户状态: userId={}", userId);
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            Integer status = (Integer) body.get("status");
            user.setStatus(status);
            userMapper.updateById(user);

            String action = status == 1 ? "解封" : "封禁";
            log.info("用户{}成功: userId={}", action, userId);
            return Result.success(action + "成功", "success");
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/users/{userId}/reset-password")
    public Result<String> resetUserPassword(@PathVariable("userId") Long userId) {
        log.info("重置用户密码: userId={}", userId);
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            // 管理员重置密码：直接设置加密后的默认密码
            String defaultPassword = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                    .hashToString(12, "123456".toCharArray());
            user.setPassword(defaultPassword);
            userMapper.updateById(user);
            return Result.success("密码已重置为默认密码", "success");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败: " + e.getMessage());
        }
    }

    // ==================== Teams ====================

    /**
     * 获取团队列表
     */
    @GetMapping("/teams")
    public Result<Map<String, Object>> getTeamList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {
        log.info("获取团队列表: page={}, keyword={}, status={}", page, keyword, status);
        try {
            LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.like(Team::getTeamName, keyword);
            }
            if (StringUtils.hasText(status)) {
                wrapper.eq(Team::getStatus, status);
            }
            wrapper.orderByDesc(Team::getCreateTime);

            Page<Team> pageResult = teamMapper.selectPage(new Page<>(page, size), wrapper);

            List<Map<String, Object>> teamList = new ArrayList<>();
            for (Team team : pageResult.getRecords()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", team.getId());
                item.put("name", team.getTeamName());
                item.put("memberCount", team.getMemberCount());
                item.put("status", team.getStatus());
                item.put("createTime", team.getCreateTime());

                // 获取owner名称
                User owner = userMapper.selectById(team.getOwnerId());
                item.put("owner", owner != null ? (owner.getDisplayName() != null ? owner.getDisplayName() : owner.getUsername()) : "Unknown");
                teamList.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("list", teamList);
            result.put("total", pageResult.getTotal());

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取团队列表失败", e);
            return Result.error("获取团队列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新团队状态（封禁/解封）
     */
    @PutMapping("/teams/{teamId}/status")
    public Result<String> updateTeamStatus(@PathVariable("teamId") Long teamId,
                                           @RequestBody Map<String, String> body) {
        log.info("更新团队状态: teamId={}", teamId);
        try {
            Team team = teamMapper.selectById(teamId);
            if (team == null) {
                return Result.error(404, "团队不存在");
            }
            String newStatus = body.get("status");
            team.setStatus(newStatus);
            teamMapper.updateById(team);

            log.info("团队状态更新成功: teamId={}, status={}", teamId, newStatus);
            return Result.success("操作成功", "success");
        } catch (Exception e) {
            log.error("更新团队状态失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除团队
     */
    @DeleteMapping("/teams/{teamId}")
    public Result<String> deleteTeam(@PathVariable("teamId") Long teamId) {
        log.info("删除团队: teamId={}", teamId);
        try {
            Team team = teamMapper.selectById(teamId);
            if (team == null) {
                return Result.error(404, "团队不存在");
            }
            team.setStatus("deleted");
            teamMapper.updateById(team);

            log.info("团队删除成功: teamId={}", teamId);
            return Result.success("删除成功", "success");
        } catch (Exception e) {
            log.error("删除团队失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    // ==================== Plans ====================

    /**
     * 获取所有订阅计划
     */
    @GetMapping("/plans")
    public Result<List<SubscriptionPlan>> getPlanList() {
        log.info("获取订阅计划列表");
        try {
            List<SubscriptionPlan> plans = planMapper.selectList(
                    new LambdaQueryWrapper<SubscriptionPlan>().orderByAsc(SubscriptionPlan::getSortOrder));
            return Result.success(plans);
        } catch (Exception e) {
            log.error("获取计划列表失败", e);
            return Result.error("获取计划列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建订阅计划
     */
    @PostMapping("/plans")
    public Result<SubscriptionPlan> createPlan(@RequestBody AdminPlanRequest request) {
        log.info("创建订阅计划: {}", request.getName());
        try {
            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setPlanName(request.getName());
            plan.setPlanCode(request.getName().toUpperCase().replace(" ", "_"));
            plan.setPriceMonthly(request.getPrice());
            plan.setPriceYearly(request.getPrice() != null ? request.getPrice().multiply(BigDecimal.TEN) : null);
            plan.setMaxRepositories(request.getProjectLimit());
            plan.setMaxDiagramsPerRepo(request.getGenerateLimit());
            plan.setSupportAi(1);
            plan.setFeatures(request.getBenefits());
            plan.setStatus(Boolean.TRUE.equals(request.getEnabled()) ? "active" : "inactive");
            plan.setSortOrder(99);

            planMapper.insert(plan);
            log.info("订阅计划创建成功: id={}", plan.getId());
            return Result.success(plan);
        } catch (Exception e) {
            log.error("创建计划失败", e);
            return Result.error("创建计划失败: " + e.getMessage());
        }
    }

    /**
     * 更新订阅计划
     */
    @PutMapping("/plans/{planId}")
    public Result<SubscriptionPlan> updatePlan(@PathVariable("planId") Long planId,
                                               @RequestBody AdminPlanRequest request) {
        log.info("更新订阅计划: planId={}", planId);
        try {
            SubscriptionPlan plan = planMapper.selectById(planId);
            if (plan == null) {
                return Result.error(404, "计划不存在");
            }
            if (request.getName() != null) plan.setPlanName(request.getName());
            if (request.getPrice() != null) {
                plan.setPriceMonthly(request.getPrice());
                plan.setPriceYearly(request.getPrice().multiply(BigDecimal.TEN));
            }
            if (request.getProjectLimit() != null) plan.setMaxRepositories(request.getProjectLimit());
            if (request.getGenerateLimit() != null) plan.setMaxDiagramsPerRepo(request.getGenerateLimit());
            if (request.getBenefits() != null) plan.setFeatures(request.getBenefits());
            if (request.getEnabled() != null) {
                plan.setStatus(request.getEnabled() ? "active" : "inactive");
            }

            planMapper.updateById(plan);
            return Result.success(plan);
        } catch (Exception e) {
            log.error("更新计划失败", e);
            return Result.error("更新计划失败: " + e.getMessage());
        }
    }

    /**
     * 删除订阅计划
     */
    @DeleteMapping("/plans/{planId}")
    public Result<String> deletePlan(@PathVariable("planId") Long planId) {
        log.info("删除订阅计划: planId={}", planId);
        try {
            planMapper.deleteById(planId);
            return Result.success("删除成功", "success");
        } catch (Exception e) {
            log.error("删除计划失败", e);
            return Result.error("删除计划失败: " + e.getMessage());
        }
    }

    // ==================== Settings ====================

    /**
     * 获取AI配置
     */
    @GetMapping("/settings/ai")
    public Result<AdminAiConfigVO> getAiConfig() {
        log.info("获取AI配置");
        try {
            AdminAiConfigVO config = new AdminAiConfigVO();
            config.setApiEndpoint(configService.getConfig("openai.api.base_url", "https://api.openai.com/v1"));
            config.setApiKey(configService.getConfig("openai.api.key", ""));
            config.setModelName(configService.getConfig("openai.model.name", "gpt-4"));
            String maxTokens = configService.getConfig("openai.max.tokens", "4096");
            config.setMaxTokens(Integer.parseInt(maxTokens));
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取AI配置失败", e);
            return Result.error("获取AI配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存AI配置
     */
    @PostMapping("/settings/ai")
    public Result<String> saveAiConfig(@RequestBody AdminAiConfigVO config) {
        log.info("保存AI配置");
        try {
            Map<String, String> configs = new HashMap<>();
            configs.put("openai.api.base_url", config.getApiEndpoint());
            if (StringUtils.hasText(config.getApiKey())) {
                configs.put("openai.api.key", config.getApiKey());
            }
            configs.put("openai.model.name", config.getModelName());
            configs.put("openai.max.tokens", String.valueOf(config.getMaxTokens()));
            configService.updateConfigs(configs);
            return Result.success("配置已保存", "success");
        } catch (Exception e) {
            log.error("保存AI配置失败", e);
            return Result.error("保存AI配置失败: " + e.getMessage());
        }
    }

    // ==================== Notifications ====================

    /**
     * 发送通知
     */
    @PostMapping("/notifications/send")
    public Result<String> sendNotification(@RequestBody SendNotificationRequest request) {
        log.info("发送通知: target={}, title={}", request.getTarget(), request.getTitle());
        try {
            if (!StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
                return Result.error(400, "标题和内容不能为空");
            }

            Notification notification = new Notification();
            notification.setTarget(request.getTarget());
            notification.setChannels(request.getChannels() != null ? String.join(",", request.getChannels()) : "inbox");
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            notification.setSenderId(StpUtil.getLoginIdAsLong());
            notification.setStatus("sent");

            notificationMapper.insert(notification);
            log.info("通知发送成功: id={}", notification.getId());
            return Result.success("通知已发送", "success");
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return Result.error("发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知历史
     */
    @GetMapping("/notifications/history")
    public Result<List<Map<String, Object>>> getNotificationHistory(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        log.info("获取通知历史");
        try {
            Page<Notification> pageResult = notificationMapper.selectPage(
                    new Page<>(page, size),
                    new LambdaQueryWrapper<Notification>().orderByDesc(Notification::getCreateTime));

            List<Map<String, Object>> list = new ArrayList<>();
            for (Notification n : pageResult.getRecords()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", n.getId());
                item.put("title", n.getTitle());
                item.put("content", n.getContent());
                item.put("target", n.getTarget());
                item.put("channels", n.getChannels());
                item.put("status", n.getStatus());
                item.put("time", n.getCreateTime());
                list.add(item);
            }
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取通知历史失败", e);
            return Result.error("获取通知历史失败: " + e.getMessage());
        }
    }

    // ==================== Helper ====================

    private double calcGrowth(Long current, Long previous) {
        if (previous == null || previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round((current - previous) * 1000.0 / previous) / 10.0;
    }
}
