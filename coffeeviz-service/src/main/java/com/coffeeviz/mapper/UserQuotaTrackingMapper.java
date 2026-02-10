package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.UserQuotaTracking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户配额跟踪 Mapper 接口
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Mapper
public interface UserQuotaTrackingMapper extends BaseMapper<UserQuotaTracking> {
}
