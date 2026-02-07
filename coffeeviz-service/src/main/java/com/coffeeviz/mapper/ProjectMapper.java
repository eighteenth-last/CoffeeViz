package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目 Mapper 接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    // BaseMapper 已提供基础 CRUD 方法
}
