package com.mini.ofbiz.autoconfig.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Mini-OFBiz实体引擎自动配置类
 * 负责自动注入核心Bean
 */
@Configuration
public class MiniOfbizAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MiniOfbizAutoConfiguration.class);

    // 后续任务将在此添加Bean定义
    // - ModelReader
    // - Delegator
    // - ConnectionFactory
    // - AutoDdlProcessor
    // - 权限相关Bean

}
