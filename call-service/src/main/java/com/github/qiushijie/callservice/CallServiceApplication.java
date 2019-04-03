package com.github.qiushijie.callservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@RestController
public class CallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallServiceApplication.class, args);
    }

    @Autowired
    private HelloService helloService;

    @RequestMapping("/hi")
    public String hello() {
        return helloService.sayHiFromClientOne();
    }

}
