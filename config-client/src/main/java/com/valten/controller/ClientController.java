package com.valten.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ClientController {

    @Value("${hello}")
    private String hello;

    @GetMapping("/hello")
    public String hello() {
        return this.hello;
    }

}
