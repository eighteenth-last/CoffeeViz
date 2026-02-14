package com.coffeeviz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffeeviz.entity.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * API 调用日志 Mapper
 */
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {

    /**
     * 按天统计 API 调用次数
     */
    @Select("SELECT call_date AS callDate, COUNT(*) AS cnt FROM sys_api_call_log " +
            "WHERE call_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY call_date ORDER BY call_date")
    List<Map<String, Object>> countByDateRange(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
