package com.mini.ofbiz.core.parser;

import com.mini.ofbiz.core.exception.EntityNotFoundException;
import com.mini.ofbiz.core.model.ModelEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据读取器
 * 全局访问入口，负责加载、缓存、查询所有实体模型
 */
public class ModelReader {

    private static final Logger log = LoggerFactory.getLogger(ModelReader.class);

    /** 实体名 -> ModelEntity 缓存（线程安全） */
    private final Map<String, ModelEntity> entityCache = new ConcurrentHashMap<>();

    /** XML解析器 */
    private final XmlEntityParser parser;

    /** 是否已初始化 */
    private volatile boolean initialized = false;

    // ================== 构造方法 ==================

    public ModelReader() {
        this.parser = new XmlEntityParser();
    }

    public ModelReader(XmlEntityParser parser) {
        this.parser = parser;
    }

    // ================== 初始化 ==================

    /**
     * 初始化：从指定路径加载所有实体模型
     *
     * @param locations XML文件位置（支持classpath*:前缀）
     */
    public synchronized void initialize(String... locations) {
        if (initialized) {
            log.warn("ModelReader already initialized, skipping");
            return;
        }

        if (locations == null || locations.length == 0) {
            log.warn("No entity locations specified");
            initialized = true;
            return;
        }

        log.info("Loading entity models from {} locations", locations.length);

        for (String location : locations) {
            loadFromLocation(location);
        }

        initialized = true;
        log.info("ModelReader initialized with {} entities", entityCache.size());
    }

    /**
     * 从指定位置加载实体模型
     */
    private void loadFromLocation(String location) {
        log.debug("Loading entities from: {}", location);

        try {
            // 解析classpath*:前缀，支持扫描多个JAR
            String searchPattern = location;
            if (searchPattern.startsWith("classpath*:")) {
                searchPattern = searchPattern.substring("classpath*:".length());
            }

            Enumeration<URL> resources = getClass().getClassLoader().getResources(searchPattern);
            List<URL> urls = new ArrayList<>();
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }

            if (urls.isEmpty()) {
                log.warn("No entity files found at: {}", location);
                return;
            }

            for (URL url : urls) {
                loadFromUrl(url);
            }

        } catch (IOException e) {
            log.error("Failed to load entities from: {}", location, e);
        }
    }

    /**
     * 从URL加载实体模型
     */
    private void loadFromUrl(URL url) {
        log.debug("Loading entities from URL: {}", url);

        try (InputStream is = url.openStream()) {
            List<ModelEntity> entities = parser.parse(is);
            for (ModelEntity entity : entities) {
                String entityName = entity.getEntityName();
                if (entityCache.containsKey(entityName)) {
                    log.warn("Duplicate entity definition: {}, overwriting", entityName);
                }
                entityCache.put(entityName, entity);
                log.debug("Loaded entity: {} -> {}", entityName, entity.getTableName());
            }
        } catch (Exception e) {
            log.error("Failed to load entities from URL: {}", url, e);
        }
    }

    // ================== 查询方法 ==================

    /**
     * 根据实体名获取模型
     *
     * @param entityName 实体名
     * @return ModelEntity
     * @throws EntityNotFoundException 实体不存在
     */
    public ModelEntity getModelEntity(String entityName) {
        if (entityName == null || entityName.isEmpty()) {
            throw new IllegalArgumentException("entityName cannot be null or empty");
        }

        ModelEntity entity = entityCache.get(entityName);
        if (entity == null) {
            throw new EntityNotFoundException(entityName,
                    "Entity '" + entityName + "' not found. Available entities: " + entityCache.keySet());
        }
        return entity;
    }

    /**
     * 根据实体名获取模型（不存在返回null）
     *
     * @param entityName 实体名
     * @return ModelEntity or null
     */
    public ModelEntity getModelEntityOrNull(String entityName) {
        if (entityName == null || entityName.isEmpty()) {
            return null;
        }
        return entityCache.get(entityName);
    }

    /**
     * 检查实体是否存在
     */
    public boolean exists(String entityName) {
        return entityName != null && entityCache.containsKey(entityName);
    }

    /**
     * 获取所有实体名
     */
    public Set<String> getAllEntityNames() {
        return Collections.unmodifiableSet(entityCache.keySet());
    }

    /**
     * 获取所有实体模型
     */
    public Collection<ModelEntity> getAllModelEntities() {
        return Collections.unmodifiableCollection(entityCache.values());
    }

    /**
     * 获取实体数量
     */
    public int getEntityCount() {
        return entityCache.size();
    }

    /**
     * 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    // ================== 缓存管理 ==================

    /**
     * 清空缓存
     */
    public synchronized void clear() {
        entityCache.clear();
        initialized = false;
        log.info("ModelReader cache cleared");
    }

    /**
     * 手动添加实体模型
     */
    public void addModelEntity(ModelEntity entity) {
        if (entity == null || entity.getEntityName() == null) {
            throw new IllegalArgumentException("Entity or entityName cannot be null");
        }
        entityCache.put(entity.getEntityName(), entity);
        log.debug("Added entity model: {}", entity.getEntityName());
    }

    /**
     * 移除实体模型
     */
    public ModelEntity removeModelEntity(String entityName) {
        ModelEntity removed = entityCache.remove(entityName);
        if (removed != null) {
            log.debug("Removed entity model: {}", entityName);
        }
        return removed;
    }

    // ================== 工具方法 ==================

    /**
     * 根据表名查找实体
     */
    public ModelEntity findByTableName(String tableName) {
        if (tableName == null) {
            return null;
        }
        return entityCache.values().stream()
                .filter(e -> tableName.equalsIgnoreCase(e.getTableName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据包名过滤实体
     */
    public List<ModelEntity> findByPackage(String packageName) {
        if (packageName == null) {
            return new ArrayList<>();
        }
        return entityCache.values().stream()
                .filter(e -> packageName.equals(e.getPackageName()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public String toString() {
        return "ModelReader{" +
                "initialized=" + initialized +
                ", entityCount=" + entityCache.size() +
                ", entities=" + entityCache.keySet() +
                '}';
    }
}
