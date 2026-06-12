package com.mini.ofbiz.core.model;

import java.io.Serializable;
import java.util.*;

/**
 * 索引元数据模型
 * 描述数据库索引结构
 */
public class ModelIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 索引名 */
    private String name;

    /** 索引字段列表 */
    private final List<String> fieldNames = new ArrayList<>();

    /** 是否唯一索引 */
    private boolean unique;

    // ================== 构造方法 ==================

    public ModelIndex() {
    }

    public ModelIndex(String name) {
        this.name = name;
    }

    public ModelIndex(String name, boolean unique) {
        this.name = name;
        this.unique = unique;
    }

    // ================== 字段管理 ==================

    /**
     * 添加索引字段
     */
    public void addFieldName(String fieldName) {
        if (fieldName != null && !fieldName.isEmpty() && !fieldNames.contains(fieldName)) {
            fieldNames.add(fieldName);
        }
    }

    /**
     * 批量添加索引字段
     */
    public void addFieldNames(String... names) {
        for (String name : names) {
            addFieldName(name);
        }
    }

    /**
     * 获取索引字段数量
     */
    public int getFieldCount() {
        return fieldNames.size();
    }

    /**
     * 是否为复合索引
     */
    public boolean isComposite() {
        return fieldNames.size() > 1;
    }

    // ================== 链式Setter ==================

    public ModelIndex name(String name) {
        this.name = name;
        return this;
    }

    public ModelIndex unique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public ModelIndex field(String fieldName) {
        addFieldName(fieldName);
        return this;
    }

    public ModelIndex fields(String... fieldNames) {
        addFieldNames(fieldNames);
        return this;
    }

    // ================== Getters & Setters ==================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFieldNames() {
        return Collections.unmodifiableList(fieldNames);
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    // ================== Object 方法 ==================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelIndex that = (ModelIndex) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ModelIndex{" +
                "name='" + name + '\'' +
                ", fields=" + fieldNames +
                ", unique=" + unique +
                '}';
    }
}
