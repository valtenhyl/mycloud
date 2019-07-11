package com.valten.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderHandler {

    @Value("${server.port}")
    private String port;


    @GetMapping("/hello")
    public String hello() {
        return "provider的端口是：" + port;
    }
}
