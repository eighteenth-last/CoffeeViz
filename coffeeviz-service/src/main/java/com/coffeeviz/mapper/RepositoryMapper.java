package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.Repository;
import org.apache.ibatis.annotations.Mapper;

/**
 * 架构库 Mapper
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Mapper
public interface RepositoryMapper extends BaseMapper<Repository> {
}
