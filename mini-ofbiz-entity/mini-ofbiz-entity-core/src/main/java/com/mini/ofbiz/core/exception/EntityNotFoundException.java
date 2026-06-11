package com.mini.ofbiz.core.exception;

/**
 * 实体未找到异常
 * 当通过实体名查找元数据或数据记录不存在时抛出
 */
public class EntityNotFoundException extends EntityException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String entityName) {
        super(entityName, "Entity definition not found: " + entityName);
    }

    public EntityNotFoundException(String entityName, String message) {
        super(entityName, message);
    }

    public EntityNotFoundException(String entityName, String message, Throwable cause) {
        super(entityName, message, cause);
    }
}
