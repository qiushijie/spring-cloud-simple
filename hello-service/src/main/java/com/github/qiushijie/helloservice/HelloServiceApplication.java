package com.github.qiushijie.helloservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class HelloServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloServiceApplication.class, args);
    }

    @Value("${server.port}")
    private int port;

    @RequestMapping("/hello/hi")
    public String aa() {
        return "aa";
    }

    @RequestMapping("/hi")
    public String hello() {
        return "hi, my port=" + port;
    }

}
