package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队成员 Mapper
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
    
    /**
     * 查询团队成员列表（包含用户信息）
     */
    @Select("SELECT tm.*, u.username, u.email, u.avatar_url, u.display_name " +
            "FROM biz_team_member tm " +
            "LEFT JOIN sys_user u ON tm.user_id = u.id " +
            "WHERE tm.team_id = #{teamId} AND tm.status = 'active' " +
            "ORDER BY tm.role DESC, tm.join_time ASC")
    List<TeamMember> selectTeamMembersWithUser(@Param("teamId") Long teamId);
}
