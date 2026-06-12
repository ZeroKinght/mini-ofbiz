package com.mini.ofbiz.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping
public class HelloController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return Map.of(
            "message", "Mini-OFBiz Entity Engine is running!",
            "status", "OK"
        );
    }

}
