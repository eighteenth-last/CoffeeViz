package com.coffeeviz.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Configuration
public class MybatisPlusConfig {
    
    /**
     * MyBatis-Plus 拦截器配置
     * 包含分页插件
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = 
            new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置最大分页数，防止恶意查询
        paginationInterceptor.setMaxLimit(500L);
        // 设置溢出总页数后是否进行处理（默认不处理）
        paginationInterceptor.setOverflow(false);
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}
