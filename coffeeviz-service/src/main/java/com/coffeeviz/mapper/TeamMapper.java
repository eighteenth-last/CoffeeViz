package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队 Mapper
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    
    /**
     * 查询用户的团队列表（包含角色信息）
     */
    @Select("SELECT t.*, tm.role as user_role, u.username as owner_name, r.repository_name " +
            "FROM biz_team t " +
            "LEFT JOIN biz_team_member tm ON t.id = tm.team_id AND tm.user_id = #{userId} " +
            "LEFT JOIN sys_user u ON t.owner_id = u.id " +
            "LEFT JOIN biz_repository r ON t.repository_id = r.id " +
            "WHERE tm.user_id = #{userId} AND tm.status = 'active' AND t.status = 'active' " +
            "ORDER BY t.update_time DESC")
    List<Team> selectUserTeams(@Param("userId") Long userId);
    
    /**
     * 查询团队详情（包含角色信息）
     */
    @Select("SELECT t.*, tm.role as user_role, u.username as owner_name, r.repository_name " +
            "FROM biz_team t " +
            "LEFT JOIN biz_team_member tm ON t.id = tm.team_id AND tm.user_id = #{userId} " +
            "LEFT JOIN sys_user u ON t.owner_id = u.id " +
            "LEFT JOIN biz_repository r ON t.repository_id = r.id " +
            "WHERE t.id = #{teamId}")
    Team selectTeamWithRole(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
