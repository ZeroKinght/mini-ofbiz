package com.mini.ofbiz.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 字段语义类型枚举
 * OFBiz标准语义类型定义，独立于具体数据库
 * 引擎自动映射到对应的数据库类型
 */
public enum FieldType {

    // ================== ID类型 ==================

    /** 主键ID（可为空） */
    ID("id", "VARCHAR(20)", String.class),

    /** 非空主键ID */
    ID_NE("id-ne", "VARCHAR(20)", String.class),

    /** 长ID（可为空） */
    ID_LONG("id-long", "VARCHAR(40)", String.class),

    /** 非空长ID */
    ID_LONG_NE("id-long-ne", "VARCHAR(40)", String.class),

    // ================== 名称类型 ==================

    /** 名称 */
    NAME("name", "VARCHAR(100)", String.class),

    /** 描述 */
    DESCRIPTION("description", "VARCHAR(255)", String.class),

    /** 短名称 */
    SHORT_NAME("short-name", "VARCHAR(60)", String.class),

    /** 非空名称 */
    NAME_NE("name-ne", "VARCHAR(100)", String.class),

    // ================== 数值类型 ==================

    /** 金额（精确小数） */
    VALUE("value", "DECIMAL(18,2)", java.math.BigDecimal.class),

    /** 浮点数 */
    FLOAT("float", "DOUBLE", Double.class),

    /** 整数 */
    INTEGER("integer", "INT", Integer.class),

    /** 长整数 */
    LONG("long", "BIGINT", Long.class),

    /** 百分比 */
    PERCENT("percent", "DECIMAL(10,2)", java.math.BigDecimal.class),

    // ================== 日期时间类型 ==================

    /** 日期 */
    DATE("date", "DATE", java.time.LocalDate.class),

    /** 日期时间 */
    DATE_TIME("date-time", "DATETIME", java.time.LocalDateTime.class),

    /** 时间 */
    TIME("time", "TIME", java.time.LocalTime.class),

    /** 时间戳 */
    TIMESTAMP("timestamp", "TIMESTAMP", java.time.LocalDateTime.class),

    // ================== 文本类型 ==================

    /** 长文本 */
    TEXT("text", "TEXT", String.class),

    /** 超长字符串 */
    VERY_LONG("very-long", "VARCHAR(2000)", String.class),

    /** 评论 */
    COMMENT("comment", "VARCHAR(500)", String.class),

    // ================== 标识类型 ==================

    /** 布尔标识 Y/N */
    INDICATOR("indicator", "CHAR(1)", String.class),

    /** 状态码 */
    STATUS("status", "VARCHAR(20)", String.class),

    // ================== 其他类型 ==================

    /** 二进制 */
    BLOB("blob", "LONGBLOB", byte[].class),

    /** 电子邮箱 */
    EMAIL("email", "VARCHAR(100)", String.class),

    /** URL */
    URL("url", "VARCHAR(500)", String.class),

    /** 电话号码 */
    PHONE("phone", "VARCHAR(30)", String.class),

    /** IP地址 */
    IP_ADDRESS("ip-address", "VARCHAR(50)", String.class);

    // ================== 枚举属性 ==================

    /** 语义类型标识 */
    private final String semantic;

    /** 默认SQL类型 */
    private final String defaultSqlType;

    /** 对应Java类型 */
    private final Class<?> javaClass;

    /** 语义类型 -> FieldType 映射缓存 */
    private static final Map<String, FieldType> SEMANTIC_MAP = new HashMap<>();

    static {
        for (FieldType ft : values()) {
            SEMANTIC_MAP.put(ft.semantic, ft);
        }
    }

    FieldType(String semantic, String defaultSqlType, Class<?> javaClass) {
        this.semantic = semantic;
        this.defaultSqlType = defaultSqlType;
        this.javaClass = javaClass;
    }

    // ================== 静态方法 ==================

    /**
     * 根据语义类型获取FieldType
     */
    public static FieldType fromSemantic(String semantic) {
        FieldType ft = SEMANTIC_MAP.get(semantic);
        if (ft == null) {
            throw new IllegalArgumentException("Unknown field type: " + semantic);
        }
        return ft;
    }

    /**
     * 判断语义类型是否存在
     */
    public static boolean exists(String semantic) {
        return SEMANTIC_MAP.containsKey(semantic);
    }

    // ================== 实例方法 ==================

    /**
     * 获取SQL类型（支持自定义长度）
     */
    public String getSqlType(Integer length) {
        if (length == null || length <= 0) {
            return defaultSqlType;
        }
        // 对于有长度参数的类型，生成带长度的SQL类型
        String upperType = defaultSqlType.toUpperCase();
        if (upperType.startsWith("VARCHAR")) {
            return "VARCHAR(" + length + ")";
        } else if (upperType.startsWith("CHAR")) {
            return "CHAR(" + length + ")";
        } else if (upperType.startsWith("DECIMAL")) {
            // 对于DECIMAL，格式为 precision,scale
            int scale = Math.min(10, length); // 小数位数
            int precision = length; // 总位数
            return "DECIMAL(" + precision + "," + scale + ")";
        }
        return defaultSqlType;
    }

    /**
     * 判断是否为日期时间类型
     */
    public boolean isDateTimeType() {
        return this == DATE || this == DATE_TIME || this == TIME || this == TIMESTAMP;
    }

    /**
     * 判断是否为数值类型
     */
    public boolean isNumericType() {
        return this == VALUE || this == FLOAT || this == INTEGER || this == LONG || this == PERCENT;
    }

    /**
     * 判断是否为文本类型
     */
    public boolean isTextType() {
        return this == TEXT || this == VERY_LONG || this == COMMENT;
    }

    /**
     * 判断是否为ID类型
     */
    public boolean isIdType() {
        return this == ID || this == ID_NE || this == ID_LONG || this == ID_LONG_NE;
    }

    /**
     * 判断是否必须非空（_NE后缀）
     */
    public boolean isNotNullByDefault() {
        return semantic.endsWith("-ne");
    }

    // ================== Getters ==================

    public String getSemantic() {
        return semantic;
    }

    public String getDefaultSqlType() {
        return defaultSqlType;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    @Override
    public String toString() {
        return "FieldType{" +
                "semantic='" + semantic + '\'' +
                ", sqlType='" + defaultSqlType + '\'' +
                ", javaClass=" + javaClass.getSimpleName() +
                '}';
    }
}
