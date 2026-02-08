package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.Diagram;
import org.apache.ibatis.annotations.Mapper;

/**
 * 架构图 Mapper
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Mapper
public interface DiagramMapper extends BaseMapper<Diagram> {
}
