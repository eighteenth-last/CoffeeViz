package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.TeamInvitationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请使用记录 Mapper
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface TeamInvitationLogMapper extends BaseMapper<TeamInvitationLog> {
}
