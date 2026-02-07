package com.coffeeviz.export.exception;

/**
 * 导出异常
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public class ExportException extends Exception {
    
    public ExportException(String message) {
        super(message);
    }
    
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
