package com.mini.ofbiz.core.parser;

import com.mini.ofbiz.core.exception.EntityException;
import com.mini.ofbiz.core.model.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * XML实体定义解析器
 * 基于Dom4j解析entitymodel.xml文件，构建ModelEntity对象
 */
public class XmlEntityParser {

    private static final Logger log = LoggerFactory.getLogger(XmlEntityParser.class);

    /** SAX解析器（线程安全，可复用） */
    private final SAXReader saxReader;

    public XmlEntityParser() {
        this.saxReader = new SAXReader();
        // 禁用外部实体解析，防止XXE攻击
        saxReader.setEntityResolver((publicId, systemId) -> null);
    }

    /**
     * 解析单个XML输入流
     *
     * @param inputStream XML文件输入流
     * @return 解析出的实体列表
     * @throws EntityException 解析异常
     */
    public List<ModelEntity> parse(InputStream inputStream) throws EntityException {
        if (inputStream == null) {
            throw new EntityException("Input stream is null");
        }

        try {
            Document document = saxReader.read(inputStream);
            Element root = document.getRootElement();
            return parseEntities(root);
        } catch (DocumentException e) {
            throw new EntityException("Failed to parse XML document", e);
        }
    }

    /**
     * 解析根元素下所有entity节点
     */
    private List<ModelEntity> parseEntities(Element root) {
        List<ModelEntity> entities = new ArrayList<>();

        Iterator<Element> it = root.elementIterator("entity");
        while (it.hasNext()) {
            Element entityElement = it.next();
            try {
                ModelEntity entity = parseEntity(entityElement);
                if (entity != null) {
                    entities.add(entity);
                    log.debug("Parsed entity: {}", entity.getEntityName());
                }
            } catch (Exception e) {
                log.error("Failed to parse entity element", e);
            }
        }

        log.info("Parsed {} entities from XML", entities.size());
        return entities;
    }

    /**
     * 解析单个entity元素
     */
    private ModelEntity parseEntity(Element entityElement) {
        String entityName = entityElement.attributeValue("entity-name");
        String tableName = entityElement.attributeValue("table-name");
        String packageName = entityElement.attributeValue("package-name");
        if (packageName == null) {
            packageName = entityElement.attributeValue("package");
        }

        if (entityName == null || entityName.isEmpty()) {
            log.warn("Entity missing 'entity-name' attribute, skipping");
            return null;
        }

        // 表名默认为实体名的蛇形命名
        if (tableName == null || tableName.isEmpty()) {
            tableName = ModelField.nameToColName(entityName);
        }

        ModelEntity entity = new ModelEntity(entityName, tableName);
        entity.setPackageName(packageName);

        // 解析description子元素或属性
        Element descElement = entityElement.element("description");
        if (descElement != null) {
            entity.setDescription(descElement.getTextTrim());
        } else {
            entity.setDescription(entityElement.attributeValue("description"));
        }

        // 解析字段
        parseFields(entityElement, entity);

        // 解析索引
        parseIndexes(entityElement, entity);

        return entity;
    }

    /**
     * 解析entity下所有field元素
     */
    private void parseFields(Element entityElement, ModelEntity entity) {
        Iterator<Element> it = entityElement.elementIterator("field");
        while (it.hasNext()) {
            Element fieldElement = it.next();
            ModelField field = parseField(fieldElement);
            if (field != null) {
                entity.addField(field);
            }
        }

        // 验证至少有一个主键
        if (entity.getPkSize() == 0) {
            log.warn("Entity {} has no primary key defined", entity.getEntityName());
        }
    }

    /**
     * 解析单个field元素
     */
    private ModelField parseField(Element fieldElement) {
        String name = fieldElement.attributeValue("name");
        String type = fieldElement.attributeValue("type");

        if (name == null || name.isEmpty()) {
            log.warn("Field missing 'name' attribute, skipping");
            return null;
        }

        // 类型默认值
        if (type == null || type.isEmpty()) {
            type = "description";
            log.debug("Field {} missing type, using default 'description'", name);
        }

        ModelField field = new ModelField(name, type);

        // 解析列名（可覆盖默认转换）
        String colName = fieldElement.attributeValue("col-name");
        if (colName != null && !colName.isEmpty()) {
            field.setColName(colName);
        }

        // 是否主键
        String isPk = fieldElement.attributeValue("is-pk");
        if (isPk != null) {
            field.setPk(Boolean.parseBoolean(isPk));
        }

        // 是否非空
        String notNull = fieldElement.attributeValue("not-null");
        if (notNull != null) {
            field.setNotNull(Boolean.parseBoolean(notNull));
        } else if (FieldType.exists(type) && FieldType.fromSemantic(type).isNotNullByDefault()) {
            // -ne后缀类型默认非空
            field.setNotNull(true);
        }

        // 默认值
        String defaultValue = fieldElement.attributeValue("default");
        if (defaultValue != null) {
            field.setDefaultValue(defaultValue);
        }

        // 字段长度
        String lengthStr = fieldElement.attributeValue("length");
        if (lengthStr != null && !lengthStr.isEmpty()) {
            try {
                field.setLength(Integer.parseInt(lengthStr));
            } catch (NumberFormatException e) {
                log.warn("Invalid length value for field {}: {}", name, lengthStr);
            }
        }

        // 是否加密
        String encrypt = fieldElement.attributeValue("encrypt");
        if (encrypt != null) {
            field.setEncrypt(Boolean.parseBoolean(encrypt));
        }

        // 描述
        String description = fieldElement.attributeValue("description");
        if (description != null) {
            field.setDescription(description);
        }

        return field;
    }

    /**
     * 解析entity下所有index元素
     */
    private void parseIndexes(Element entityElement, ModelEntity entity) {
        Iterator<Element> it = entityElement.elementIterator("index");
        while (it.hasNext()) {
            Element indexElement = it.next();
            ModelIndex index = parseIndex(indexElement);
            if (index != null) {
                entity.addIndex(index);
            }
        }
    }

    /**
     * 解析单个index元素
     */
    private ModelIndex parseIndex(Element indexElement) {
        String name = indexElement.attributeValue("name");
        if (name == null || name.isEmpty()) {
            log.warn("Index missing 'name' attribute, skipping");
            return null;
        }

        String uniqueStr = indexElement.attributeValue("unique");
        boolean unique = Boolean.parseBoolean(uniqueStr);

        ModelIndex index = new ModelIndex(name, unique);

        // 解析索引字段
        Iterator<Element> fieldIt = indexElement.elementIterator("index-field");
        while (fieldIt.hasNext()) {
            Element indexFieldElement = fieldIt.next();
            String fieldName = indexFieldElement.attributeValue("name");
            if (fieldName != null && !fieldName.isEmpty()) {
                index.addFieldName(fieldName);
            }
        }

        if (index.getFieldCount() == 0) {
            log.warn("Index {} has no fields defined", name);
            return null;
        }

        return index;
    }
}
