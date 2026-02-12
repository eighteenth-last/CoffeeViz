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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    @Autowired
    private SysConfigGroupMapper configGroupMapper;

    @Autowired
    private PlanQuotaMapper planQuotaMapper;

    @Autowired(required = false)
    private com.coffeeviz.service.QuotaService quotaService;

    @Autowired
    private com.coffeeviz.mapper.UserQuotaTrackingMapper userQuotaTrackingMapper;

    @Autowired(required = false)
    private com.coffeeviz.service.PaymentConfigService paymentConfigService;

    @Autowired
    private EmailTemplateMapper emailTemplateMapper;

    @Autowired
    private EmailLogMapper emailLogMapper;

    @Autowired
    private com.coffeeviz.service.EmailService emailService;

    // ==================== Helper Methods ====================

    /**
     * 根据请求中的配额列表同步 biz_plan_quota 表（upsert 逻辑，避免外键冲突）
     */
    private void syncPlanQuotas(Long planId, List<AdminPlanRequest.QuotaItem> quotas) {
        // 查出该计划现有的配额记录，按 quotaType 索引
        List<PlanQuota> existing = planQuotaMapper.selectList(
                new LambdaQueryWrapper<PlanQuota>().eq(PlanQuota::getPlanId, planId));
        Map<String, PlanQuota> existingMap = existing.stream()
                .collect(Collectors.toMap(PlanQuota::getQuotaType, q -> q, (a, b) -> a));

        Set<String> incomingTypes = new HashSet<>();
        if (quotas != null) {
            for (AdminPlanRequest.QuotaItem item : quotas) {
                if (item.getQuotaType() == null) continue;
                incomingTypes.add(item.getQuotaType());
                PlanQuota old = existingMap.get(item.getQuotaType());
                if (old != null) {
                    // 更新已有记录
                    old.setQuotaLimit(item.getQuotaLimit() != null ? item.getQuotaLimit() : 0);
                    old.setResetCycle(item.getResetCycle() != null ? item.getResetCycle() : "monthly");
                    old.setDescription(item.getDescription());
                    planQuotaMapper.updateById(old);
                } else {
                    // 插入新记录
                    PlanQuota q = new PlanQuota();
                    q.setPlanId(planId);
                    q.setQuotaType(item.getQuotaType());
                    q.setQuotaLimit(item.getQuotaLimit() != null ? item.getQuotaLimit() : 0);
                    q.setResetCycle(item.getResetCycle() != null ? item.getResetCycle() : "monthly");
                    q.setDescription(item.getDescription());
                    planQuotaMapper.insert(q);
                }
            }
        }

        // 删除不再需要的配额（先把 user_quota_tracking 的引用指向 null，再删除）
        for (PlanQuota old : existing) {
            if (!incomingTypes.contains(old.getQuotaType())) {
                try {
                    planQuotaMapper.deleteById(old.getId());
                } catch (Exception e) {
                    // 外键约束阻止删除，将限额设为 0 标记为废弃
                    log.warn("配额记录有外键引用无法删除，设为0: planId={}, type={}", planId, old.getQuotaType());
                    old.setQuotaLimit(0);
                    old.setDescription("[已废弃]");
                    planQuotaMapper.updateById(old);
                }
            }
        }
    }

    /**
     * 计划配额变更后，同步所有该计划活跃用户的 quota_limit
     */
    private void syncActiveUserQuotas(Long planId) {
        if (quotaService == null) return;
        try {
            List<UserSubscription> activeSubs = subscriptionMapper.selectList(
                    new LambdaQueryWrapper<UserSubscription>()
                            .eq(UserSubscription::getPlanId, planId)
                            .eq(UserSubscription::getStatus, "active"));
            for (UserSubscription sub : activeSubs) {
                quotaService.updateUserQuotaLimits(sub.getUserId(), planId);
            }
            log.info("已同步 {} 个用户的配额限制: planId={}", activeSubs.size(), planId);
        } catch (Exception e) {
            log.warn("同步用户配额限制失败: planId={}, error={}", planId, e.getMessage());
        }
    }

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

            // AI调用次数（从配额跟踪表统计所有用户 ai_generate 的 quota_used 总和）
            List<com.coffeeviz.entity.UserQuotaTracking> aiTrackings = userQuotaTrackingMapper.selectList(
                    new LambdaQueryWrapper<com.coffeeviz.entity.UserQuotaTracking>()
                            .eq(com.coffeeviz.entity.UserQuotaTracking::getQuotaType, "ai_generate"));
            long totalAiCalls = aiTrackings.stream()
                    .mapToLong(t -> t.getQuotaUsed() != null ? t.getQuotaUsed() : 0)
                    .sum();
            stats.setAiCalls(totalAiCalls);
            stats.setAiCallGrowth(0.0);

            // Chart Data - Growth Trend (Last 7 Days)
            List<String> dates = new ArrayList<>();
            List<Long> newUsers = new ArrayList<>();
            List<Long> apiCalls = new ArrayList<>();
            
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                dates.add(date.format(formatter));
                
                // New Users
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();
                Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, start)
                        .lt(User::getCreateTime, end));
                newUsers.add(count);
                
                // API Calls (Placeholder - 0 for now as no tracking table exists)
                apiCalls.add(0L); 
            }
            stats.setChartDates(dates);
            stats.setChartNewUsers(newUsers);
            stats.setChartApiCalls(apiCalls);

            // Chart Data - Subscription Distribution
            Map<String, Long> distribution = new LinkedHashMap<>(); // Use LinkedHashMap for order
            
            // Get all plans
            List<SubscriptionPlan> plans = planMapper.selectList(
                new LambdaQueryWrapper<SubscriptionPlan>().orderByAsc(SubscriptionPlan::getSortOrder));
            
            Long totalPaid = 0L;
            String freePlanName = "Free";

            for (SubscriptionPlan plan : plans) {
                boolean isFree = "FREE".equalsIgnoreCase(plan.getPlanCode());
                if (isFree) {
                    freePlanName = plan.getPlanName();
                    // Reserve spot in LinkedHashMap to maintain sort order
                    distribution.put(freePlanName, 0L); 
                    continue; // Skip counting for free plan, will calculate as residual
                }

                // Count active subscriptions for this plan
                Long count = subscriptionMapper.selectCount(new LambdaQueryWrapper<UserSubscription>()
                        .eq(UserSubscription::getStatus, "active")
                        .eq(UserSubscription::getPlanId, plan.getId()));
                
                if (count > 0) {
                    distribution.put(plan.getPlanName(), count);
                    totalPaid += count;
                }
            }
            
            // Free Users = Total Users - Total Active Paid Subscriptions
            Long freeUsers = stats.getTotalUsers() - totalPaid;
            if (freeUsers < 0) freeUsers = 0L;
            
            // Update Free plan count
            distribution.put(freePlanName, freeUsers);
            
            stats.setSubscriptionDistribution(distribution);

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
     * 获取所有订阅计划（附带配额列表）
     */
    @GetMapping("/plans")
    public Result<List<Map<String, Object>>> getPlanList() {
        log.info("获取订阅计划列表");
        try {
            List<SubscriptionPlan> plans = planMapper.selectList(
                    new LambdaQueryWrapper<SubscriptionPlan>().orderByAsc(SubscriptionPlan::getSortOrder));
            List<Map<String, Object>> result = new ArrayList<>();
            for (SubscriptionPlan plan : plans) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", plan.getId());
                item.put("planCode", plan.getPlanCode());
                item.put("planName", plan.getPlanName());
                item.put("planNameEn", plan.getPlanNameEn());
                item.put("description", plan.getDescription());
                item.put("priceMonthly", plan.getPriceMonthly());
                item.put("priceYearly", plan.getPriceYearly());
                item.put("supportJdbc", plan.getSupportJdbc());
                item.put("supportAi", plan.getSupportAi());
                item.put("supportExport", plan.getSupportExport());
                item.put("supportTeam", plan.getSupportTeam());
                item.put("maxTeams", plan.getMaxTeams());
                item.put("maxTeamMembers", plan.getMaxTeamMembers());
                item.put("prioritySupport", plan.getPrioritySupport());
                item.put("features", plan.getFeatures());
                item.put("sortOrder", plan.getSortOrder());
                item.put("status", plan.getStatus());
                // 附带配额列表
                List<PlanQuota> quotas = planQuotaMapper.selectList(
                        new LambdaQueryWrapper<PlanQuota>().eq(PlanQuota::getPlanId, plan.getId()));
                item.put("quotas", quotas);
                result.add(item);
            }
            return Result.success(result);
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
            plan.setFeatures(request.getBenefits());
            plan.setStatus(Boolean.TRUE.equals(request.getEnabled()) ? "active" : "inactive");
            plan.setSortOrder(99);
            // 功能开关
            if (request.getSupportJdbc() != null) plan.setSupportJdbc(request.getSupportJdbc());
            if (request.getSupportAi() != null) plan.setSupportAi(request.getSupportAi());
            if (request.getSupportExport() != null) plan.setSupportExport(request.getSupportExport());
            if (request.getSupportTeam() != null) plan.setSupportTeam(request.getSupportTeam());
            if (request.getMaxTeams() != null) plan.setMaxTeams(request.getMaxTeams());
            if (request.getMaxTeamMembers() != null) plan.setMaxTeamMembers(request.getMaxTeamMembers());
            if (request.getPrioritySupport() != null) plan.setPrioritySupport(request.getPrioritySupport());

            planMapper.insert(plan);
            syncPlanQuotas(plan.getId(), request.getQuotas());
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
            if (request.getBenefits() != null) plan.setFeatures(request.getBenefits());
            if (request.getEnabled() != null) {
                plan.setStatus(request.getEnabled() ? "active" : "inactive");
            }
            // 功能开关
            if (request.getSupportJdbc() != null) plan.setSupportJdbc(request.getSupportJdbc());
            if (request.getSupportAi() != null) plan.setSupportAi(request.getSupportAi());
            if (request.getSupportExport() != null) plan.setSupportExport(request.getSupportExport());
            if (request.getSupportTeam() != null) plan.setSupportTeam(request.getSupportTeam());
            if (request.getMaxTeams() != null) plan.setMaxTeams(request.getMaxTeams());
            if (request.getMaxTeamMembers() != null) plan.setMaxTeamMembers(request.getMaxTeamMembers());
            if (request.getPrioritySupport() != null) plan.setPrioritySupport(request.getPrioritySupport());

            planMapper.updateById(plan);
            if (request.getQuotas() != null) {
                syncPlanQuotas(plan.getId(), request.getQuotas());
            }
            // 同步所有该计划活跃用户的配额限制
            syncActiveUserQuotas(planId);
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

    // ==================== Payment Config ====================

    /**
     * 获取支付配置（从三条独立记录读取）
     */
    @GetMapping("/settings/payment")
    public Result<AdminPaymentConfigVO> getPaymentConfig() {
        log.info("获取支付配置");
        try {
            java.util.List<SysConfigGroup> groups = configGroupMapper.selectList(
                    new LambdaQueryWrapper<SysConfigGroup>()
                            .likeRight(SysConfigGroup::getGroupCode, "payment_"));

            AdminPaymentConfigVO config = new AdminPaymentConfigVO();

            for (SysConfigGroup group : groups) {
                String code = group.getGroupCode();
                boolean enabled = group.getStatus() != null && group.getStatus() == 1;
                com.alibaba.fastjson2.JSONObject json = StringUtils.hasText(group.getConfigValue())
                        ? com.alibaba.fastjson2.JSON.parseObject(group.getConfigValue())
                        : new com.alibaba.fastjson2.JSONObject();

                switch (code) {
                    case "payment_wechat" -> {
                        AdminPaymentConfigVO.WechatPayConfig wc = json.toJavaObject(AdminPaymentConfigVO.WechatPayConfig.class);
                        if (wc == null) wc = new AdminPaymentConfigVO.WechatPayConfig();
                        wc.setEnabled(enabled);
                        config.setWechatPay(wc);
                    }
                    case "payment_alipay" -> {
                        AdminPaymentConfigVO.AlipayConfig ac = json.toJavaObject(AdminPaymentConfigVO.AlipayConfig.class);
                        if (ac == null) ac = new AdminPaymentConfigVO.AlipayConfig();
                        ac.setEnabled(enabled);
                        config.setAlipay(ac);
                    }
                    case "payment_test" -> {
                        AdminPaymentConfigVO.TestConfig tc = json.toJavaObject(AdminPaymentConfigVO.TestConfig.class);
                        if (tc == null) tc = new AdminPaymentConfigVO.TestConfig();
                        tc.setEnabled(enabled);
                        config.setTest(tc);
                    }
                }
            }

            // 确保返回非null对象
            if (config.getWechatPay() == null) {
                AdminPaymentConfigVO.WechatPayConfig wc = new AdminPaymentConfigVO.WechatPayConfig();
                wc.setEnabled(false);
                config.setWechatPay(wc);
            }
            if (config.getAlipay() == null) {
                AdminPaymentConfigVO.AlipayConfig ac = new AdminPaymentConfigVO.AlipayConfig();
                ac.setEnabled(false);
                ac.setSignType("RSA2");
                ac.setCharset("UTF-8");
                ac.setGatewayUrl("https://openapi.alipay.com/gateway.do");
                config.setAlipay(ac);
            }
            if (config.getTest() == null) {
                AdminPaymentConfigVO.TestConfig tc = new AdminPaymentConfigVO.TestConfig();
                tc.setEnabled(false);
                tc.setDescription("测试环境模拟支付，无需真实支付");
                config.setTest(tc);
            }

            return Result.success(config);
        } catch (Exception e) {
            log.error("获取支付配置失败", e);
            return Result.error("获取支付配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存支付配置（分别保存三条独立记录，status 控制启用/禁用）
     */
    @PostMapping("/settings/payment")
    public Result<String> savePaymentConfig(@RequestBody AdminPaymentConfigVO config) {
        log.info("保存支付配置");
        try {
            // 保存微信支付
            if (config.getWechatPay() != null) {
                savePaymentGroup("payment_wechat", "微信支付", "wechat",
                        config.getWechatPay(), Boolean.TRUE.equals(config.getWechatPay().getEnabled()), 1);
            }
            // 保存支付宝
            if (config.getAlipay() != null) {
                savePaymentGroup("payment_alipay", "支付宝", "alipay",
                        config.getAlipay(), Boolean.TRUE.equals(config.getAlipay().getEnabled()), 2);
            }
            // 保存测试支付
            if (config.getTest() != null) {
                savePaymentGroup("payment_test", "测试支付", "test",
                        config.getTest(), Boolean.TRUE.equals(config.getTest().getEnabled()), 3);
            }

            refreshPaymentRuntime();
            return Result.success("支付配置已保存", "success");
        } catch (Exception e) {
            log.error("保存支付配置失败", e);
            return Result.error("保存支付配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存单条支付配置记录
     */
    private void savePaymentGroup(String groupCode, String groupName, String icon,
                                   Object configObj, boolean enabled, int sort) {
        // 序列化时排除 enabled 字段（enabled 由 status 字段控制）
        String jsonStr = com.alibaba.fastjson2.JSON.toJSONString(configObj);
        com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(jsonStr);
        json.remove("enabled"); // enabled 存在 status 字段，不重复存 JSON 里

        SysConfigGroup group = configGroupMapper.selectOne(
                new LambdaQueryWrapper<SysConfigGroup>().eq(SysConfigGroup::getGroupCode, groupCode));

        if (group == null) {
            group = new SysConfigGroup();
            group.setGroupCode(groupCode);
            group.setGroupName(groupName);
            group.setGroupIcon(icon);
            group.setConfigValue(json.toJSONString());
            group.setStatus(enabled ? 1 : 0);
            group.setSort(sort);
            configGroupMapper.insert(group);
        } else {
            group.setConfigValue(json.toJSONString());
            group.setStatus(enabled ? 1 : 0);
            configGroupMapper.updateById(group);
        }
    }

    /**
     * 保存后刷新运行时支付配置
     */
    private void refreshPaymentRuntime() {
        if (paymentConfigService != null) {
            paymentConfigService.refreshPaymentConfig();
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

    // ==================== Email Config ====================

    /**
     * 获取邮件配置
     */
    @GetMapping("/settings/email")
    public Result<AdminEmailConfigVO> getEmailConfig() {
        log.info("获取邮件配置");
        try {
            SysConfigGroup group = configGroupMapper.selectOne(
                    new LambdaQueryWrapper<SysConfigGroup>().eq(SysConfigGroup::getGroupCode, "email"));
            AdminEmailConfigVO vo = new AdminEmailConfigVO();
            if (group != null && group.getConfigValue() != null) {
                com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(group.getConfigValue());
                vo = json.toJavaObject(AdminEmailConfigVO.class);
                if (vo == null) vo = new AdminEmailConfigVO();
            }
            vo.setEnabled(group != null && group.getStatus() != null && group.getStatus() == 1);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取邮件配置失败", e);
            return Result.error("获取邮件配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存邮件配置
     */
    @PostMapping("/settings/email")
    public Result<String> saveEmailConfig(@RequestBody AdminEmailConfigVO config) {
        log.info("保存邮件配置");
        try {
            String jsonStr = com.alibaba.fastjson2.JSON.toJSONString(config);
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(jsonStr);
            json.remove("enabled");

            SysConfigGroup group = configGroupMapper.selectOne(
                    new LambdaQueryWrapper<SysConfigGroup>().eq(SysConfigGroup::getGroupCode, "email"));
            if (group == null) {
                group = new SysConfigGroup();
                group.setGroupCode("email");
                group.setGroupName("邮件推送");
                group.setGroupIcon("mail");
                group.setConfigValue(json.toJSONString());
                group.setStatus(Boolean.TRUE.equals(config.getEnabled()) ? 1 : 0);
                group.setSort(10);
                configGroupMapper.insert(group);
            } else {
                group.setConfigValue(json.toJSONString());
                group.setStatus(Boolean.TRUE.equals(config.getEnabled()) ? 1 : 0);
                configGroupMapper.updateById(group);
            }
            return Result.success("邮件配置已保存", "success");
        } catch (Exception e) {
            log.error("保存邮件配置失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 发送测试邮件
     */
    @PostMapping("/email/test")
    public Result<String> sendTestEmail(@RequestBody java.util.Map<String, String> body) {
        String toEmail = body.get("email");
        if (!StringUtils.hasText(toEmail)) return Result.error(400, "请输入测试邮箱");
        String msg = emailService.sendTestEmail(toEmail);
        return msg.startsWith("发送失败") ? Result.error(msg) : Result.success(msg, "success");
    }

    /**
     * 发送邮件（手动）
     */
    @PostMapping("/email/send")
    public Result<String> sendEmail(@RequestBody SendEmailRequest request) {
        log.info("管理员发送邮件: target={}, template={}", request.getTarget(), request.getTemplateCode());
        emailService.sendEmail(request);
        return Result.success("邮件发送任务已提交", "success");
    }

    // ==================== Email Templates ====================

    /**
     * 获取邮件模板列表
     */
    @GetMapping("/email/templates")
    public Result<List<EmailTemplate>> getEmailTemplates() {
        List<EmailTemplate> list = emailTemplateMapper.selectList(
                new LambdaQueryWrapper<EmailTemplate>().orderByAsc(EmailTemplate::getId));
        return Result.success(list);
    }

    /**
     * 创建/更新邮件模板
     */
    @PostMapping("/email/templates")
    public Result<EmailTemplate> saveEmailTemplate(@RequestBody EmailTemplate template) {
        if (template.getId() != null) {
            emailTemplateMapper.updateById(template);
        } else {
            emailTemplateMapper.insert(template);
        }
        return Result.success(template);
    }

    /**
     * 删除邮件模板
     */
    @DeleteMapping("/email/templates/{id}")
    public Result<String> deleteEmailTemplate(@PathVariable("id") Long id) {
        emailTemplateMapper.deleteById(id);
        return Result.success("删除成功", "success");
    }

    // ==================== Email Logs ====================

    /**
     * 获取邮件发送日志
     */
    @GetMapping("/email/logs")
    public Result<java.util.Map<String, Object>> getEmailLogs(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Page<EmailLog> pageResult = emailLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<EmailLog>().orderByDesc(EmailLog::getCreateTime));
        java.util.Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    // ==================== Helper ====================

    private double calcGrowth(Long current, Long previous) {
        if (previous == null || previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round((current - previous) * 1000.0 / previous) / 10.0;
    }
}
