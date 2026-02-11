package com.coffeeviz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.common.exception.BusinessException;
import com.coffeeviz.entity.*;
import com.coffeeviz.mapper.*;
import com.coffeeviz.service.TeamService;
import com.coffeeviz.service.SubscriptionService;
import com.coffeeviz.service.UserService;
import com.coffeeviz.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 团队服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamInvitationMapper teamInvitationMapper;
    private final TeamInvitationLogMapper teamInvitationLogMapper;
    private final TeamLogMapper teamLogMapper;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final RepositoryService repositoryService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Team createTeam(String teamName, Long repositoryId, String description, String avatarUrl, Long userId) {
        log.info("创建团队: teamName={}, repositoryId={}, userId={}", teamName, repositoryId, userId);
        
        // 1. 检查用户是否有团队订阅权限
        checkTeamSubscription(userId);
        
        // 2. 获取用户订阅信息，确定最大成员数和团队数量
        UserSubscription subscription = subscriptionService.getUserActiveSubscription(userId);
        SubscriptionPlan plan = subscriptionService.getPlanById(subscription.getPlanId());
        
        // 3. 检查用户已创建的团队数量
        LambdaQueryWrapper<Team> teamQuery = new LambdaQueryWrapper<>();
        teamQuery.eq(Team::getOwnerId, userId)
                .eq(Team::getStatus, "active");
        long teamCount = teamMapper.selectCount(teamQuery);
        
        if (teamCount >= plan.getMaxTeams()) {
            throw new BusinessException("已达到最大团队数量限制: " + plan.getMaxTeams());
        }
        
        // 4. 检查归档库是否存在且属于该用户
        Repository repository = repositoryService.getRepositoryById(repositoryId, userId);
        if (repository == null) {
            throw new BusinessException("归档库不存在");
        }
        if (!repository.getUserId().equals(userId)) {
            throw new BusinessException("只能绑定自己的归档库");
        }
        
        // 5. 检查归档库是否已被其他团队绑定
        LambdaQueryWrapper<Team> repoQuery = new LambdaQueryWrapper<>();
        repoQuery.eq(Team::getRepositoryId, repositoryId)
                .eq(Team::getStatus, "active");
        Team existingTeam = teamMapper.selectOne(repoQuery);
        if (existingTeam != null) {
            throw new BusinessException("该归档库已被团队绑定");
        }
        
        // 6. 生成团队唯一标识
        String teamCode = generateTeamCode();
        
        // 7. 创建团队
        Team team = new Team();
        team.setTeamName(teamName);
        team.setTeamCode(teamCode);
        team.setDescription(description);
        team.setAvatarUrl(avatarUrl);
        team.setOwnerId(userId);
        team.setRepositoryId(repositoryId);
        team.setMemberCount(1);
        team.setMaxMembers(plan.getMaxTeamMembers());
        team.setStatus("active");
        team.setCreateTime(LocalDateTime.now());
        team.setUpdateTime(LocalDateTime.now());
        
        teamMapper.insert(team);
        
        // 8. 添加所有者为团队成员
        TeamMember ownerMember = new TeamMember();
        ownerMember.setTeamId(team.getId());
        ownerMember.setUserId(userId);
        ownerMember.setRole("owner");
        ownerMember.setJoinTime(LocalDateTime.now());
        ownerMember.setJoinSource("create");
        ownerMember.setStatus("active");
        ownerMember.setCreateTime(LocalDateTime.now());
        ownerMember.setUpdateTime(LocalDateTime.now());
        
        teamMemberMapper.insert(ownerMember);
        
        // 9. 更新归档库标记为团队归档库
        repository.setIsTeamRepository(true);
        repositoryService.updateRepository(repository);
        
        // 10. 记录操作日志
        logTeamAction(team.getId(), userId, "create_team", "team", team.getId(),
                "创建团队: " + teamName, null, null);
        
        log.info("团队创建成功: teamId={}, teamCode={}", team.getId(), teamCode);
        return team;
    }
    
    @Override
    public Team getTeamById(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在");
        }
        return team;
    }
    
    @Override
    public List<Team> getUserTeams(Long userId) {
        // 查询用户作为成员的所有团队
        LambdaQueryWrapper<TeamMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, "active");
        List<TeamMember> members = teamMemberMapper.selectList(memberQuery);
        
        if (members.isEmpty()) {
            return List.of();
        }
        
        // 获取团队ID列表
        List<Long> teamIds = members.stream()
                .map(TeamMember::getTeamId)
                .toList();
        
        // 查询团队信息
        return teamMapper.selectBatchIds(teamIds);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeam(Long teamId, String teamName, String description, String avatarUrl, Long userId) {
        log.info("更新团队: teamId={}, userId={}", teamId, userId);
        
        // 检查权限
        if (!isTeamOwner(teamId, userId)) {
            throw new BusinessException("只有团队所有者可以更新团队信息");
        }
        
        Team team = getTeamById(teamId);
        team.setTeamName(teamName);
        team.setDescription(description);
        team.setAvatarUrl(avatarUrl);
        team.setUpdateTime(LocalDateTime.now());
        
        teamMapper.updateById(team);
        
        // 记录操作日志
        logTeamAction(teamId, userId, "update_team", "team", teamId,
                "更新团队信息", null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeam(Long teamId, Long userId) {
        log.info("删除团队: teamId={}, userId={}", teamId, userId);
        
        // 检查权限
        if (!isTeamOwner(teamId, userId)) {
            throw new BusinessException("只有团队所有者可以删除团队");
        }
        
        Team team = getTeamById(teamId);
        
        // 软删除团队
        team.setStatus("deleted");
        team.setUpdateTime(LocalDateTime.now());
        teamMapper.updateById(team);
        
        // 停用所有成员
        LambdaQueryWrapper<TeamMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(TeamMember::getTeamId, teamId);
        List<TeamMember> members = teamMemberMapper.selectList(memberQuery);
        
        for (TeamMember member : members) {
            member.setStatus("inactive");
            member.setUpdateTime(LocalDateTime.now());
            teamMemberMapper.updateById(member);
        }
        
        // 禁用所有邀请链接
        LambdaQueryWrapper<TeamInvitation> inviteQuery = new LambdaQueryWrapper<>();
        inviteQuery.eq(TeamInvitation::getTeamId, teamId);
        List<TeamInvitation> invitations = teamInvitationMapper.selectList(inviteQuery);
        
        for (TeamInvitation invitation : invitations) {
            invitation.setStatus("disabled");
            invitation.setUpdateTime(LocalDateTime.now());
            teamInvitationMapper.updateById(invitation);
        }
        
        // 更新归档库标记
        Repository repository = repositoryService.getRepositoryById(team.getRepositoryId(), team.getOwnerId());
        if (repository != null) {
            repository.setIsTeamRepository(false);
            repositoryService.updateRepository(repository);
        }
        
        // 记录操作日志
        logTeamAction(teamId, userId, "delete_team", "team", teamId,
                "删除团队", null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamInvitation createInviteLink(Long teamId, Integer maxUses, LocalDateTime expireTime, Long userId) {
        log.info("创建邀请链接: teamId={}, userId={}", teamId, userId);
        
        // 检查权限
        if (!isTeamOwner(teamId, userId)) {
            throw new BusinessException("只有团队所有者可以创建邀请链接");
        }
        
        Team team = getTeamById(teamId);
        
        // 生成邀请码
        String inviteCode = UUID.randomUUID().toString().replace("-", "");
        
        // 生成邀请链接（这里使用占位符，实际应该从配置读取）
        String inviteUrl = "https://coffeeviz.pantoria.cn/team/join/" + inviteCode;
        
        // 创建邀请记录
        TeamInvitation invitation = new TeamInvitation();
        invitation.setTeamId(teamId);
        invitation.setInviteCode(inviteCode);
        invitation.setInviteUrl(inviteUrl);
        invitation.setCreatorId(userId);
        invitation.setMaxUses(maxUses == null ? 0 : maxUses);
        invitation.setUsedCount(0);
        invitation.setStatus("active");
        invitation.setExpireTime(expireTime);
        invitation.setCreateTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());
        
        teamInvitationMapper.insert(invitation);
        
        // 记录操作日志
        logTeamAction(teamId, userId, "create_invite", "invitation", invitation.getId(),
                "创建邀请链接", null, null);
        
        return invitation;
    }
    
    @Override
    public List<TeamInvitation> getTeamInviteLinks(Long teamId) {
        LambdaQueryWrapper<TeamInvitation> query = new LambdaQueryWrapper<>();
        query.eq(TeamInvitation::getTeamId, teamId)
                .orderByDesc(TeamInvitation::getCreateTime);
        return teamInvitationMapper.selectList(query);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableInviteLink(Long inviteLinkId, Long userId) {
        TeamInvitation invitation = teamInvitationMapper.selectById(inviteLinkId);
        if (invitation == null) {
            throw new BusinessException("邀请链接不存在");
        }
        
        // 检查权限
        if (!isTeamOwner(invitation.getTeamId(), userId)) {
            throw new BusinessException("只有团队所有者可以禁用邀请链接");
        }
        
        invitation.setStatus("disabled");
        invitation.setUpdateTime(LocalDateTime.now());
        teamInvitationMapper.updateById(invitation);
        
        // 记录操作日志
        logTeamAction(invitation.getTeamId(), userId, "disable_invite", "invitation", inviteLinkId,
                "禁用邀请链接", null, null);
    }
    
    @Override
    public TeamInvitation getInviteLinkByCode(String inviteCode) {
        LambdaQueryWrapper<TeamInvitation> query = new LambdaQueryWrapper<>();
        query.eq(TeamInvitation::getInviteCode, inviteCode);
        TeamInvitation invitation = teamInvitationMapper.selectOne(query);
        
        if (invitation == null) {
            throw new BusinessException("邀请链接不存在");
        }
        
        // 检查邀请链接状态
        if (!"active".equals(invitation.getStatus())) {
            throw new BusinessException("邀请链接已失效");
        }
        
        // 检查是否过期
        if (invitation.getExpireTime() != null && LocalDateTime.now().isAfter(invitation.getExpireTime())) {
            invitation.setStatus("expired");
            teamInvitationMapper.updateById(invitation);
            throw new BusinessException("邀请链接已过期");
        }
        
        // 检查使用次数
        if (invitation.getMaxUses() > 0 && invitation.getUsedCount() >= invitation.getMaxUses()) {
            throw new BusinessException("邀请链接使用次数已达上限");
        }
        
        return invitation;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinTeamByInviteCode(String inviteCode, Long userId) {
        log.info("用户加入团队: inviteCode={}, userId={}", inviteCode, userId);
        
        // 验证邀请链接
        TeamInvitation invitation = getInviteLinkByCode(inviteCode);
        Team team = getTeamById(invitation.getTeamId());
        
        // 检查用户是否已是团队成员
        if (isTeamMember(team.getId(), userId)) {
            throw new BusinessException("您已经是该团队成员");
        }
        
        // 检查团队成员数量限制
        if (team.getMemberCount() >= team.getMaxMembers()) {
            throw new BusinessException("团队成员已达上限");
        }
        
        // 添加成员
        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("member");
        member.setJoinTime(LocalDateTime.now());
        member.setJoinSource("invite");
        member.setInviteCode(inviteCode);
        member.setStatus("active");
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        
        teamMemberMapper.insert(member);
        
        // 更新团队成员数量
        team.setMemberCount(team.getMemberCount() + 1);
        team.setUpdateTime(LocalDateTime.now());
        teamMapper.updateById(team);
        
        // 更新邀请链接使用次数
        invitation.setUsedCount(invitation.getUsedCount() + 1);
        invitation.setUpdateTime(LocalDateTime.now());
        teamInvitationMapper.updateById(invitation);
        
        // 记录邀请使用日志
        User user = userService.getUserById(userId);
        TeamInvitationLog inviteLog = new TeamInvitationLog();
        inviteLog.setTeamId(team.getId());
        inviteLog.setInviteCode(inviteCode);
        inviteLog.setUserId(userId);
        inviteLog.setUsername(user.getUsername());
        inviteLog.setEmail(user.getEmail());
        inviteLog.setIsNewUser(false);
        inviteLog.setJoinTime(LocalDateTime.now());
        
        teamInvitationLogMapper.insert(inviteLog);
        
        // 记录操作日志
        logTeamAction(team.getId(), userId, "join_team", "member", member.getId(),
                "加入团队", null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerAndJoinTeam(String inviteCode, String username, String password, String email,
                                   String ipAddress, String userAgent) {
        log.info("注册并加入团队: inviteCode={}, username={}", inviteCode, username);
        
        // 验证邀请链接
        TeamInvitation invitation = getInviteLinkByCode(inviteCode);
        Team team = getTeamById(invitation.getTeamId());
        
        // 检查团队成员数量限制
        if (team.getMemberCount() >= team.getMaxMembers()) {
            throw new BusinessException("团队成员已达上限");
        }
        
        // 注册新用户
        User newUser = userService.register(username, password, email, null);
        Long userId = newUser.getId();
        
        // 添加成员
        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("member");
        member.setJoinTime(LocalDateTime.now());
        member.setJoinSource("invite");
        member.setInviteCode(inviteCode);
        member.setStatus("active");
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        
        teamMemberMapper.insert(member);
        
        // 更新团队成员数量
        team.setMemberCount(team.getMemberCount() + 1);
        team.setUpdateTime(LocalDateTime.now());
        teamMapper.updateById(team);
        
        // 更新邀请链接使用次数
        invitation.setUsedCount(invitation.getUsedCount() + 1);
        invitation.setUpdateTime(LocalDateTime.now());
        teamInvitationMapper.updateById(invitation);
        
        // 记录邀请使用日志
        TeamInvitationLog inviteLog = new TeamInvitationLog();
        inviteLog.setTeamId(team.getId());
        inviteLog.setInviteCode(inviteCode);
        inviteLog.setUserId(userId);
        inviteLog.setUsername(username);
        inviteLog.setEmail(email);
        inviteLog.setIsNewUser(true);
        inviteLog.setJoinTime(LocalDateTime.now());
        inviteLog.setIpAddress(ipAddress);
        inviteLog.setUserAgent(userAgent);
        
        teamInvitationLogMapper.insert(inviteLog);
        
        // 记录操作日志
        logTeamAction(team.getId(), userId, "register_and_join", "member", member.getId(),
                "注册并加入团队", ipAddress, userAgent);
        
        return userId;
    }
    
    @Override
    public List<TeamMember> getTeamMembers(Long teamId) {
        LambdaQueryWrapper<TeamMember> query = new LambdaQueryWrapper<>();
        query.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, "active")
                .orderByAsc(TeamMember::getJoinTime);
        return teamMemberMapper.selectList(query);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long teamId, Long memberId, Long operatorId) {
        log.info("移除团队成员: teamId={}, memberId={}, operatorId={}", teamId, memberId, operatorId);
        
        // 检查权限
        if (!isTeamOwner(teamId, operatorId)) {
            throw new BusinessException("只有团队所有者可以移除成员");
        }
        
        TeamMember member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getTeamId().equals(teamId)) {
            throw new BusinessException("成员不存在");
        }
        
        // 不能移除所有者
        if ("owner".equals(member.getRole())) {
            throw new BusinessException("不能移除团队所有者");
        }
        
        // 停用成员
        member.setStatus("inactive");
        member.setUpdateTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
        
        // 更新团队成员数量
        Team team = getTeamById(teamId);
        team.setMemberCount(team.getMemberCount() - 1);
        team.setUpdateTime(LocalDateTime.now());
        teamMapper.updateById(team);
        
        // 记录操作日志
        logTeamAction(teamId, operatorId, "remove_member", "member", memberId,
                "移除成员: " + member.getUserId(), null, null);
    }
    
    @Override
    public Repository getTeamRepository(Long teamId) {
        Team team = getTeamById(teamId);
        // 这里需要获取团队所有者ID来调用 getRepositoryById
        return repositoryService.getRepositoryById(team.getRepositoryId(), team.getOwnerId());
    }
    
    @Override
    public boolean canSaveDiagramToTeam(Long teamId, Long userId) {
        return isTeamMember(teamId, userId);
    }
    
    @Override
    public boolean isTeamOwner(Long teamId, Long userId) {
        LambdaQueryWrapper<TeamMember> query = new LambdaQueryWrapper<>();
        query.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getRole, "owner")
                .eq(TeamMember::getStatus, "active");
        return teamMemberMapper.selectCount(query) > 0;
    }
    
    @Override
    public boolean isTeamMember(Long teamId, Long userId) {
        LambdaQueryWrapper<TeamMember> query = new LambdaQueryWrapper<>();
        query.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, "active");
        return teamMemberMapper.selectCount(query) > 0;
    }
    
    @Override
    public void checkTeamSubscription(Long userId) {
        UserSubscription subscription = subscriptionService.getUserActiveSubscription(userId);
        if (subscription == null) {
            throw new BusinessException("请先订阅套餐");
        }
        
        SubscriptionPlan plan = subscriptionService.getPlanById(subscription.getPlanId());
        if (plan.getSupportTeam() == null || plan.getSupportTeam() == 0) {
            throw new BusinessException("当前订阅套餐不支持团队功能，请升级到 TEAM 或 ENTERPRISE 套餐");
        }
    }
    
    @Override
    public void logTeamAction(Long teamId, Long userId, String action, String targetType, Long targetId,
                             String description, String ipAddress, String userAgent) {
        TeamLog log = new TeamLog();
        log.setTeamId(teamId);
        log.setUserId(userId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setCreateTime(LocalDateTime.now());
        
        teamLogMapper.insert(log);
    }
    
    /**
     * 生成团队唯一标识
     */
    private String generateTeamCode() {
        return "TEAM_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
}
