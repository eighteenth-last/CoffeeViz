package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.UsageQuota;
import org.apache.ibatis.annotations.Mapper;

/**
 * 使用配额 Mapper
 * 
 * @deprecated 此 Mapper 已废弃，请使用 {@link PlanQuotaMapper} 和 {@link UserQuotaTrackingMapper} 替代
 * @see PlanQuotaMapper
 * @see UserQuotaTrackingMapper
 */
@Deprecated
@Mapper
public interface UsageQuotaMapper extends BaseMapper<UsageQuota> {
}
