package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.UsageQuota;
import org.apache.ibatis.annotations.Mapper;

/**
 * 使用配额 Mapper
 */
@Mapper
public interface UsageQuotaMapper extends BaseMapper<UsageQuota> {
}
