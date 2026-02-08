package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.Diagram;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 架构图 Mapper
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Mapper
public interface DiagramMapper extends BaseMapper<Diagram> {
    
    /**
     * 统计用户所有架构图的表总数
     */
    @Select("SELECT COALESCE(SUM(d.table_count), 0) FROM biz_diagram d " +
            "INNER JOIN biz_repository r ON d.repository_id = r.id " +
            "WHERE r.user_id = #{userId}")
    Long sumTableCountByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户所有架构图的关系总数
     */
    @Select("SELECT COALESCE(SUM(d.relation_count), 0) FROM biz_diagram d " +
            "INNER JOIN biz_repository r ON d.repository_id = r.id " +
            "WHERE r.user_id = #{userId}")
    Long sumRelationCountByUserId(@Param("userId") Long userId);
}
