package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置分组实体（支持JSON格式的分组配置）
 */
@Data
@TableName("sys_config_group")
public class SysConfigGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组编码（唯一） */
    private String groupCode;

    /** 分组名称 */
    private String groupName;

    /** 分组图标 */
    private String groupIcon;

    /** 配置值（JSON格式） */
    private String configValue;

    /** 排序 */
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
