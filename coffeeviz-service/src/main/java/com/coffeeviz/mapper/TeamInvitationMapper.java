package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.TeamInvitation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队邀请链接 Mapper
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Mapper
public interface TeamInvitationMapper extends BaseMapper<TeamInvitation> {
}
