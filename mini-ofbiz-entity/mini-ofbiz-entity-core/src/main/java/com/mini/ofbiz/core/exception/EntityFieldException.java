package com.mini.ofbiz.core.exception;

/**
 * 字段异常
 * 当字段校验失败、类型转换错误等情况抛出
 */
public class EntityFieldException extends EntityException {

    private static final long serialVersionUID = 1L;

    /** 字段名 */
    private String fieldName;

    public EntityFieldException(String entityName, String fieldName, String message) {
        super(entityName, message);
        this.fieldName = fieldName;
    }

    public EntityFieldException(String entityName, String fieldName, String message, Throwable cause) {
        super(entityName, message, cause);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        String entityName = getEntityName();
        String prefix = "[Entity:" + entityName + ", Field:" + fieldName + "] ";
        return (message != null) ? (s + ": " + prefix + message) : s;
    }
}
