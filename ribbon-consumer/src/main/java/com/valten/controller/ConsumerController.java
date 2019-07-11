package com.valten.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

    @Autowired
    RestTemplate restTemplate;


    @GetMapping("/ribbon-consumer")
    public String hello() {
        return restTemplate.getForEntity("http://PROVIDER-SERVICE/hello", String.class).getBody();
    }
}
