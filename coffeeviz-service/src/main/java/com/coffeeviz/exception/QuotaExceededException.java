package com.coffeeviz.exception;

/**
 * 配额超出异常
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
public class QuotaExceededException extends RuntimeException {
    
    private String quotaType;
    private Integer currentUsed;
    private Integer limit;
    
    public QuotaExceededException(String quotaType, Integer currentUsed, Integer limit) {
        super(String.format("配额类型 %s 的配额限制已超出：已使用 %d，限制 %d", 
            quotaType, currentUsed, limit));
        this.quotaType = quotaType;
        this.currentUsed = currentUsed;
        this.limit = limit;
    }
    
    public String getQuotaType() {
        return quotaType;
    }
    
    public Integer getCurrentUsed() {
        return currentUsed;
    }
    
    public Integer getLimit() {
        return limit;
    }
}
