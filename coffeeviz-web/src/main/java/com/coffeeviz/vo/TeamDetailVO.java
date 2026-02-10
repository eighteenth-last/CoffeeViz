package com.coffeeviz.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队详情 VO
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class TeamDetailVO {
    
    private Long id;
    private String teamName;
    private String teamCode;
    private String description;
    private String avatarUrl;
    private Long ownerId;
    private String ownerName;
    private Long repositoryId;
    private String repositoryName;
    private Integer memberCount;
    private Integer maxMembers;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 当前用户在团队中的角色
    private String userRole;
    
    // 团队成员列表
    private java.util.List<TeamMemberVO> members;
    
    // 权限标识
    private Boolean canEdit;
    private Boolean canManageMembers;
    private Boolean canDelete;
}
