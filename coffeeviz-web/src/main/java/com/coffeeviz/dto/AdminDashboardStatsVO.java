package com.coffeeviz.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 管理端仪表盘统计数据
 */
@Data
public class AdminDashboardStatsVO {
    private Long totalUsers;
    private Double userGrowth;
    private Long activeTeams;
    private Double teamGrowth;
    private BigDecimal monthlyRevenue;
    private Double revenueGrowth;
    private Long aiCalls;
    private Double aiCallGrowth;
}
