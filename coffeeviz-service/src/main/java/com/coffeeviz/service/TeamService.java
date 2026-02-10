package com.coffeeviz.service;

import com.coffeeviz.entity.Team;
import com.coffeeviz.entity.TeamInvitation;
import com.coffeeviz.entity.TeamMember;
import com.coffeeviz.entity.Repository;

import java.util.List;

/**
 * 团队服务接口
 */
public interface TeamService {
    
    /**
     * 创建团队
     * @param teamName 团队名称
     * @param repositoryId 归档库ID
     * @param description 团队描述
     * @param avatarUrl 团队头像
     * @param userId 创建人ID
     * @return 团队信息
     */
    Team createTeam(String teamName, Long repositoryId, String description, String avatarUrl, Long userId);
    
    /**
     * 获取团队详情
     * @param teamId 团队ID
     * @return 团队信息
     */
    Team getTeamById(Long teamId);
    
    /**
     * 获取用户的团队列表
     * @param userId 用户ID
     * @return 团队列表
     */
    List<Team> getUserTeams(Long userId);
    
    /**
     * 更新团队信息
     * @param teamId 团队ID
     * @param teamName 团队名称
     * @param description 团队描述
     * @param avatarUrl 团队头像
     * @param userId 操作人ID
     */
    void updateTeam(Long teamId, String teamName, String description, String avatarUrl, Long userId);
    
    /**
     * 删除团队
     * @param teamId 团队ID
     * @param userId 操作人ID
     */
    void deleteTeam(Long teamId, Long userId);
    
    /**
     * 生成邀请链接
     * @param teamId 团队ID
     * @param maxUses 最大使用次数（0表示无限制）
     * @param expireTime 过期时间（null表示永久有效）
     * @param userId 创建人ID
     * @return 邀请链接信息
     */
    TeamInvitation createInviteLink(Long teamId, Integer maxUses, java.time.LocalDateTime expireTime, Long userId);
    
    /**
     * 获取团队的邀请链接列表
     * @param teamId 团队ID
     * @return 邀请链接列表
     */
    List<TeamInvitation> getTeamInviteLinks(Long teamId);
    
    /**
     * 禁用邀请链接
     * @param inviteLinkId 邀请链接ID
     * @param userId 操作人ID
     */
    void disableInviteLink(Long inviteLinkId, Long userId);
    
    /**
     * 根据邀请码获取邀请链接信息
     * @param inviteCode 邀请码
     * @return 邀请链接信息
     */
    TeamInvitation getInviteLinkByCode(String inviteCode);
    
    /**
     * 通过邀请码加入团队（已登录用户）
     * @param inviteCode 邀请码
     * @param userId 用户ID
     */
    void joinTeamByInviteCode(String inviteCode, Long userId);
    
    /**
     * 注册并加入团队（新用户）
     * @param inviteCode 邀请码
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 新用户ID
     */
    Long registerAndJoinTeam(String inviteCode, String username, String password, String email, 
                            String ipAddress, String userAgent);
    
    /**
     * 获取团队成员列表
     * @param teamId 团队ID
     * @return 成员列表
     */
    List<TeamMember> getTeamMembers(Long teamId);
    
    /**
     * 移除团队成员
     * @param teamId 团队ID
     * @param memberId 成员ID
     * @param operatorId 操作人ID
     */
    void removeMember(Long teamId, Long memberId, Long operatorId);
    
    /**
     * 获取团队归档库
     * @param teamId 团队ID
     * @return 归档库信息
     */
    Repository getTeamRepository(Long teamId);
    
    /**
     * 检查用户是否可以保存架构图到团队
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 是否可以保存
     */
    boolean canSaveDiagramToTeam(Long teamId, Long userId);
    
    /**
     * 检查用户是否是团队所有者
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 是否是所有者
     */
    boolean isTeamOwner(Long teamId, Long userId);
    
    /**
     * 检查用户是否是团队成员
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 是否是成员
     */
    boolean isTeamMember(Long teamId, Long userId);
    
    /**
     * 检查用户是否有团队订阅权限
     * @param userId 用户ID
     * @throws com.coffeeviz.common.exception.BusinessException 如果没有权限
     */
    void checkTeamSubscription(Long userId);
    
    /**
     * 记录团队操作日志
     * @param teamId 团队ID
     * @param userId 操作人ID
     * @param action 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param description 操作描述
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void logTeamAction(Long teamId, Long userId, String action, String targetType, Long targetId,
                      String description, String ipAddress, String userAgent);
}
