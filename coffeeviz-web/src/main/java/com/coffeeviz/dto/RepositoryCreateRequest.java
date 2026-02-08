package com.coffeeviz.dto;

import lombok.Data;

/**
 * 创建架构库请求
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
public class RepositoryCreateRequest {
    
    /**
     * 架构库名称
     */
    private String repositoryName;
    
    /**
     * 架构库描述
     */
    private String description;
}
