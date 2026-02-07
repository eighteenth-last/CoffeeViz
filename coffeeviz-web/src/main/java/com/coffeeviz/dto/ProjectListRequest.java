package com.coffeeviz.dto;

import lombok.Data;

/**
 * 项目列表查询请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class ProjectListRequest {
    
    /**
     * 页码（默认 1）
     */
    private Integer page = 1;
    
    /**
     * 每页大小（默认 10）
     */
    private Integer size = 10;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 状态筛选
     */
    private String status;
}
