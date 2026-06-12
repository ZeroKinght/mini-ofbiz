package com.mini.ofbiz.core.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体元数据模型
 * 封装一个XML实体定义的全部信息
 */
public class ModelEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 实体名（Java风格，如 "SysUser"） */
    private String entityName;

    /** 表名（数据库风格，如 "sys_user"） */
    private String tableName;

    /** 包名（用于分组） */
    private String packageName;

    /** 字段列表（按定义顺序） */
    private final List<ModelField> fields = new ArrayList<>();

    /** 主键字段列表（从fields中提取） */
    private final List<ModelField> pkFields = new ArrayList<>();

    /** 索引列表 */
    private final List<ModelIndex> indexes = new ArrayList<>();

    /** 描述 */
    private String description;

    /** 字段名 -> ModelField 快速查询映射 */
    private final Map<String, ModelField> fieldMap = new LinkedHashMap<>();

    // ================== 构造方法 ==================

    public ModelEntity() {
    }

    public ModelEntity(String entityName, String tableName) {
        this.entityName = entityName;
        this.tableName = tableName;
    }

    // ================== 字段管理 ==================

    /**
     * 添加字段
     */
    public void addField(ModelField field) {
        if (field == null) {
            return;
        }
        fields.add(field);
        fieldMap.put(field.getName(), field);
        // 如果是主键，同步到pkFields
        if (field.isPk()) {
            pkFields.add(field);
        }
    }

    /**
     * 根据字段名获取字段模型
     */
    public ModelField getField(String fieldName) {
        return fieldMap.get(fieldName);
    }

    /**
     * 判断字段是否存在
     */
    public boolean hasField(String fieldName) {
        return fieldMap.containsKey(fieldName);
    }

    /**
     * 获取所有字段名
     */
    public Set<String> getFieldNames() {
        return fieldMap.keySet();
    }

    /**
     * 获取非主键字段列表
     */
    public List<ModelField> getNonPkFields() {
        return fields.stream()
                .filter(f -> !f.isPk())
                .collect(Collectors.toList());
    }

    // ================== 主键操作 ==================

    /**
     * 获取主键字段名（单主键场景）
     */
    public String getPkFieldName() {
        if (pkFields.size() == 1) {
            return pkFields.getFirst().getName();
        }
        throw new IllegalStateException(
                "Entity " + entityName + " has " + pkFields.size() + " pk fields, use getPkFieldNames() instead");
    }

    /**
     * 获取所有主键字段名
     */
    public List<String> getPkFieldNames() {
        return pkFields.stream()
                .map(ModelField::getName)
                .collect(Collectors.toList());
    }

    /**
     * 获取主键字段数量
     */
    public int getPkSize() {
        return pkFields.size();
    }

    /**
     * 是否为复合主键
     */
    public boolean isCompositePk() {
        return pkFields.size() > 1;
    }

    // ================== 索引管理 ==================

    /**
     * 添加索引
     */
    public void addIndex(ModelIndex index) {
        if (index != null) {
            indexes.add(index);
        }
    }

    /**
     * 根据索引名获取索引
     */
    public ModelIndex getIndex(String indexName) {
        return indexes.stream()
                .filter(idx -> idx.getName().equals(indexName))
                .findFirst()
                .orElse(null);
    }

    // ================== Getters & Setters ==================

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<ModelField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<ModelField> getPkFields() {
        return Collections.unmodifiableList(pkFields);
    }

    public List<ModelIndex> getIndexes() {
        return Collections.unmodifiableList(indexes);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ================== Object 方法 ==================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelEntity that = (ModelEntity) o;
        return Objects.equals(entityName, that.entityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName);
    }

    @Override
    public String toString() {
        return "ModelEntity{" +
                "entityName='" + entityName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", fields=" + fields.size() +
                ", pkFields=" + pkFields.size() +
                ", indexes=" + indexes.size() +
                '}';
    }
}
