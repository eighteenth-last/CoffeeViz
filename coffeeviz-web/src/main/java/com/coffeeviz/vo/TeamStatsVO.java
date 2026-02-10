package com.coffeeviz.vo;

import lombok.Data;

/**
 * 团队统计 VO
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class TeamStatsVO {
    
    private Long teamId;
    private Integer memberCount;
    private Integer maxMembers;
    private Integer diagramCount;
    private Integer inviteLinkCount;
    private Integer activeInviteLinkCount;
    private Integer todayJoinCount;
    private Integer weekJoinCount;
    private Integer monthJoinCount;
    private java.time.LocalDateTime createTime;
}
