package com.coffeeviz.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请链接信息 VO
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class InviteLinkInfoVO {
    
    private String inviteCode;
    private String inviteUrl;
    private Long teamId;
    private String teamName;
    private String teamDescription;
    private String teamAvatarUrl;
    private String ownerName;
    private Integer memberCount;
    private Integer maxMembers;
    private String status;
    private LocalDateTime expireTime;
    private Integer maxUses;
    private Integer usedCount;
    
    // 是否可用
    private Boolean available;
    private String unavailableReason;
}
