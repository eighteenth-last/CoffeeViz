package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.util.Result;
import com.coffeeviz.dto.CreateInviteLinkRequest;
import com.coffeeviz.dto.CreateTeamRequest;
import com.coffeeviz.dto.JoinTeamRequest;
import com.coffeeviz.dto.UpdateTeamRequest;
import com.coffeeviz.entity.*;
import com.coffeeviz.service.TeamService;
import com.coffeeviz.service.UserService;
import com.coffeeviz.vo.InviteLinkInfoVO;
import com.coffeeviz.vo.TeamDetailVO;
import com.coffeeviz.vo.TeamMemberVO;
import com.coffeeviz.vo.TeamStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    private final UserService userService;
    
    /**
     * 创建团队
     */
    @PostMapping("/create")
    public Result<Team> createTeam(@Validated @RequestBody CreateTeamRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            Team team = teamService.createTeam(
                request.getTeamName(),
                request.getRepositoryId(),
                request.getDescription(),
                request.getAvatarUrl(),
                userId
            );
            
            return Result.success(team);
        } catch (Exception e) {
            log.error("创建团队失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的团队列表
     */
    @GetMapping("/list")
    public Result<List<Team>> getUserTeams() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Team> teams = teamService.getUserTeams(userId);
            return Result.success(teams);
        } catch (Exception e) {
            log.error("获取团队列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取团队详情
     */
    @GetMapping("/{teamId}")
    public Result<TeamDetailVO> getTeamDetail(@PathVariable("teamId") Long teamId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查权限
            if (!teamService.isTeamMember(teamId, userId)) {
                return Result.error("您不是该团队成员");
            }
            
            Team team = teamService.getTeamById(teamId);
            List<TeamMember> members = teamService.getTeamMembers(teamId);
            Repository repository = teamService.getTeamRepository(teamId);
            
            // 构建 VO
            TeamDetailVO vo = new TeamDetailVO();
            vo.setId(team.getId());
            vo.setTeamName(team.getTeamName());
            vo.setTeamCode(team.getTeamCode());
            vo.setDescription(team.getDescription());
            vo.setAvatarUrl(team.getAvatarUrl());
            vo.setOwnerId(team.getOwnerId());
            vo.setRepositoryId(team.getRepositoryId());
            vo.setRepositoryName(repository.getRepositoryName());
            vo.setMemberCount(team.getMemberCount());
            vo.setMaxMembers(team.getMaxMembers());
            vo.setStatus(team.getStatus());
            vo.setCreateTime(team.getCreateTime());
            
            // 设置当前用户角色
            boolean isOwner = teamService.isTeamOwner(teamId, userId);
            vo.setUserRole(isOwner ? "owner" : "member");
            
            // 转换成员列表
            List<TeamMemberVO> memberVOs = members.stream().map(member -> {
                TeamMemberVO memberVO = new TeamMemberVO();
                memberVO.setId(member.getId());
                memberVO.setUserId(member.getUserId());
                memberVO.setRole(member.getRole());
                memberVO.setNickname(member.getNickname());
                memberVO.setJoinTime(member.getJoinTime());
                memberVO.setJoinSource(member.getJoinSource());
                memberVO.setStatus(member.getStatus());
                
                // 获取用户信息
                User user = userService.getUserById(member.getUserId());
                if (user != null) {
                    memberVO.setUsername(user.getUsername());
                    memberVO.setEmail(user.getEmail());
                    memberVO.setAvatarUrl(user.getAvatarUrl());
                }
                
                return memberVO;
            }).collect(Collectors.toList());
            
            vo.setMembers(memberVOs);
            
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取团队详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新团队信息
     */
    @PutMapping("/{teamId}")
    public Result<Void> updateTeam(@PathVariable("teamId") Long teamId, 
                                   @Validated @RequestBody UpdateTeamRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            teamService.updateTeam(
                teamId,
                request.getTeamName(),
                request.getDescription(),
                request.getAvatarUrl(),
                userId
            );
            
            return Result.success();
        } catch (Exception e) {
            log.error("更新团队失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除团队
     */
    @DeleteMapping("/{teamId}")
    public Result<Void> deleteTeam(@PathVariable("teamId") Long teamId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            teamService.deleteTeam(teamId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除团队失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 生成邀请链接
     */
    @PostMapping("/{teamId}/invite-link")
    public Result<TeamInvitation> createInviteLink(@PathVariable("teamId") Long teamId,
                                                   @RequestBody CreateInviteLinkRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            TeamInvitation invitation = teamService.createInviteLink(
                teamId,
                request.getMaxUses(),
                request.getExpireTime(),
                userId
            );
            
            return Result.success(invitation);
        } catch (Exception e) {
            log.error("生成邀请链接失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取团队的邀请链接列表
     */
    @GetMapping("/{teamId}/invite-links")
    public Result<List<TeamInvitation>> getInviteLinks(@PathVariable("teamId") Long teamId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查权限
            if (!teamService.isTeamOwner(teamId, userId)) {
                return Result.error("只有团队所有者可以查看邀请链接");
            }
            
            List<TeamInvitation> invitations = teamService.getTeamInviteLinks(teamId);
            return Result.success(invitations);
        } catch (Exception e) {
            log.error("获取邀请链接列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 禁用邀请链接
     */
    @PutMapping("/invite-link/{inviteLinkId}/disable")
    public Result<Void> disableInviteLink(@PathVariable("inviteLinkId") Long inviteLinkId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            teamService.disableInviteLink(inviteLinkId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("禁用邀请链接失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取邀请链接信息（公开接口，无需登录）
     */
    @GetMapping("/join/{inviteCode}/info")
    public Result<InviteLinkInfoVO> getInviteLinkInfo(@PathVariable("inviteCode") String inviteCode) {
        try {
            TeamInvitation invitation = teamService.getInviteLinkByCode(inviteCode);
            Team team = teamService.getTeamById(invitation.getTeamId());
            
            InviteLinkInfoVO vo = new InviteLinkInfoVO();
            vo.setInviteCode(inviteCode);
            vo.setTeamId(team.getId());
            vo.setTeamName(team.getTeamName());
            vo.setTeamDescription(team.getDescription());
            vo.setTeamAvatarUrl(team.getAvatarUrl());
            vo.setMemberCount(team.getMemberCount());
            vo.setMaxMembers(team.getMaxMembers());
            vo.setExpireTime(invitation.getExpireTime());
            vo.setMaxUses(invitation.getMaxUses());
            vo.setUsedCount(invitation.getUsedCount());
            
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取邀请链接信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 加入团队（已登录用户）
     */
    @PostMapping("/join/{inviteCode}")
    public Result<Void> joinTeam(@PathVariable("inviteCode") String inviteCode, HttpServletRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取 IP 和 User-Agent
            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            teamService.joinTeamByInviteCode(inviteCode, userId);
            
            // 记录日志
            TeamInvitation invitation = teamService.getInviteLinkByCode(inviteCode);
            teamService.logTeamAction(
                invitation.getTeamId(),
                userId,
                "join_team",
                "member",
                null,
                "通过邀请链接加入团队",
                ipAddress,
                userAgent
            );
            
            return Result.success();
        } catch (Exception e) {
            log.error("加入团队失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 注册并加入团队（新用户）
     */
    @PostMapping("/register-and-join")
    public Result<String> registerAndJoinTeam(@Validated @RequestBody JoinTeamRequest request,
                                              HttpServletRequest httpRequest) {
        try {
            // 获取 IP 和 User-Agent
            String ipAddress = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 注册并加入团队
            Long userId = teamService.registerAndJoinTeam(
                request.getInviteCode(),
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                ipAddress,
                userAgent
            );
            
            // 自动登录
            StpUtil.login(userId);
            String token = StpUtil.getTokenValue();
            
            return Result.success(token);
        } catch (Exception e) {
            log.error("注册并加入团队失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取团队成员列表
     */
    @GetMapping("/{teamId}/members")
    public Result<List<TeamMemberVO>> getTeamMembers(@PathVariable("teamId") Long teamId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查权限
            if (!teamService.isTeamMember(teamId, userId)) {
                return Result.error("您不是该团队成员");
            }
            
            List<TeamMember> members = teamService.getTeamMembers(teamId);
            
            List<TeamMemberVO> memberVOs = members.stream().map(member -> {
                TeamMemberVO vo = new TeamMemberVO();
                vo.setId(member.getId());
                vo.setUserId(member.getUserId());
                vo.setRole(member.getRole());
                vo.setNickname(member.getNickname());
                vo.setJoinTime(member.getJoinTime());
                vo.setJoinSource(member.getJoinSource());
                vo.setStatus(member.getStatus());
                
                // 获取用户信息
                User user = userService.getUserById(member.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setEmail(user.getEmail());
                    vo.setAvatarUrl(user.getAvatarUrl());
                }
                
                return vo;
            }).collect(Collectors.toList());
            
            return Result.success(memberVOs);
        } catch (Exception e) {
            log.error("获取团队成员列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除团队成员
     */
    @DeleteMapping("/{teamId}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable("teamId") Long teamId, @PathVariable("memberId") Long memberId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            teamService.removeMember(teamId, memberId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("移除团队成员失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取团队归档库
     */
    @GetMapping("/{teamId}/repository")
    public Result<Repository> getTeamRepository(@PathVariable("teamId") Long teamId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查权限
            if (!teamService.isTeamMember(teamId, userId)) {
                return Result.error("您不是该团队成员");
            }
            
            Repository repository = teamService.getTeamRepository(teamId);
            return Result.success(repository);
        } catch (Exception e) {
            log.error("获取团队归档库失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取团队统计信息
     */
    @GetMapping("/{teamId}/stats")
    public Result<TeamStatsVO> getTeamStats(@PathVariable("teamId") Long teamId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查权限
            if (!teamService.isTeamMember(teamId, userId)) {
                return Result.error("您不是该团队成员");
            }
            
            Team team = teamService.getTeamById(teamId);
            Repository repository = teamService.getTeamRepository(teamId);
            
            TeamStatsVO vo = new TeamStatsVO();
            vo.setTeamId(teamId);
            vo.setMemberCount(team.getMemberCount());
            vo.setMaxMembers(team.getMaxMembers());
            vo.setDiagramCount(repository.getDiagramCount());
            vo.setCreateTime(team.getCreateTime());
            
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取团队统计信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
