package com.valten.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderHandler {

    @Value("${server.port}")
    private String port;

    @Value("${valten}")
    private String valten;

    @Autowired
    Environment env;

    @GetMapping("/hello")
    public String hello() {
        return "order的端口：" + port;
    }

    @GetMapping("/valten")
    public String valten() {
        return this.valten;
    }

    @GetMapping("/valten2")
    public String valten2() {
        return env.getProperty("valten", "未定义");
    }
}
