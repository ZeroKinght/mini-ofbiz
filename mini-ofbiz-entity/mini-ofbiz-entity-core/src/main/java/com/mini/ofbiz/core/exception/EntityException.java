package com.mini.ofbiz.core.exception;

/**
 * 实体引擎基础异常类
 * 所有实体引擎相关异常的父类
 */
public class EntityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 关联的实体名称 */
    private String entityName;

    public EntityException(String message) {
        super(message);
    }

    public EntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityException(String entityName, String message) {
        super(message);
        this.entityName = entityName;
    }

    public EntityException(String entityName, String message, Throwable cause) {
        super(message, cause);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        if (entityName != null) {
            message = "[Entity:" + entityName + "] " + (message != null ? message : "");
        }
        return (message != null) ? (s + ": " + message) : s;
    }
}
