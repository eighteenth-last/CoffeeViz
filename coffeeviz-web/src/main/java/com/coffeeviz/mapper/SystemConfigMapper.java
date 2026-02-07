package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper 接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    // BaseMapper 已提供基础 CRUD 方法
    // 可在此添加自定义 SQL 方法
}
