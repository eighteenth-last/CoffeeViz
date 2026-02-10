package com.coffeeviz.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建团队请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class CreateTeamRequest {
    
    @NotBlank(message = "团队名称不能为空")
    @Size(max = 100, message = "团队名称不能超过100个字符")
    private String teamName;
    
    @NotNull(message = "归档库ID不能为空")
    private Long repositoryId;
    
    @Size(max = 500, message = "团队描述不能超过500个字符")
    private String description;
    
    private String avatarUrl;
}
