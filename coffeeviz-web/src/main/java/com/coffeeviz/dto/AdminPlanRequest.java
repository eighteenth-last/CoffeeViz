package com.coffeeviz.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端订阅计划请求
 */
@Data
public class AdminPlanRequest {
    private String name;
    private BigDecimal price;
    private String benefits;
    private Boolean enabled;

    /** 功能开关 */
    private Integer supportJdbc;
    private Integer supportAi;
    private Integer supportExport;
    private Integer supportTeam;
    private Integer maxTeams;
    private Integer maxTeamMembers;
    private Integer prioritySupport;

    /** 配额列表（来自 biz_plan_quota） */
    private List<QuotaItem> quotas;

    @Data
    public static class QuotaItem {
        private String quotaType;
        private Integer quotaLimit;
        private String resetCycle;
        private String description;
    }
}
