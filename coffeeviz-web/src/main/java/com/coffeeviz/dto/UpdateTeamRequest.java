package com.coffeeviz.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 更新团队请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class UpdateTeamRequest {
    
    @Size(max = 100, message = "团队名称不能超过100个字符")
    private String teamName;
    
    @Size(max = 500, message = "团队描述不能超过500个字符")
    private String description;
    
    private String avatarUrl;
}
