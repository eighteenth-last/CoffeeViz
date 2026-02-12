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
    private Integer generateLimit;
    private Integer projectLimit;
    private List<String> models;
    private String benefits;
    private Boolean enabled;
}
