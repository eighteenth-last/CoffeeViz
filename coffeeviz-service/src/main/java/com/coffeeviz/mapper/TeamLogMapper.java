package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.TeamLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队操作日志 Mapper
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface TeamLogMapper extends BaseMapper<TeamLog> {
    
    /**
     * 查询团队操作日志（包含用户信息）
     */
    @Select("SELECT tl.*, u.username, u.display_name " +
            "FROM biz_team_log tl " +
            "LEFT JOIN sys_user u ON tl.user_id = u.id " +
            "WHERE tl.team_id = #{teamId} " +
            "ORDER BY tl.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<TeamLog> selectTeamLogsWithUser(@Param("teamId") Long teamId, 
                                         @Param("offset") Integer offset, 
                                         @Param("limit") Integer limit);
    
    /**
     * 统计团队操作日志数量
     */
    @Select("SELECT COUNT(*) FROM biz_team_log WHERE team_id = #{teamId}")
    Long countTeamLogs(@Param("teamId") Long teamId);
}
