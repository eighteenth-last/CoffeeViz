package com.coffeeviz.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队成员 VO
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class TeamMemberVO {
    
    private Long id;
    private Long teamId;
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String role;
    private String nickname;
    private LocalDateTime joinTime;
    private String joinSource;
    private String status;
}
