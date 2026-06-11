package com.mini.ofbiz.core.exception;

/**
 * SQL生成/执行异常
 * 当SQL生成失败或执行SQL时发生错误抛出
 */
public class EntitySqlException extends EntityException {

    private static final long serialVersionUID = 1L;

    /** 生成的SQL语句 */
    private String sql;

    public EntitySqlException(String entityName, String message) {
        super(entityName, message);
    }

    public EntitySqlException(String entityName, String message, Throwable cause) {
        super(entityName, message, cause);
    }

    public EntitySqlException(String entityName, String sql, String message, Throwable cause) {
        super(entityName, message, cause);
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        StringBuilder sb = new StringBuilder();
        sb.append(s).append(": ");
        if (getEntityName() != null) {
            sb.append("[Entity:").append(getEntityName()).append("] ");
        }
        if (message != null) {
            sb.append(message);
        }
        if (sql != null) {
            sb.append("\n  SQL: ").append(sql);
        }
        return sb.toString();
    }
}
