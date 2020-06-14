package com.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HelloServiceFallback implements HelloService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String sayHiFromClientOne() {
        logger.error("sayHiFromClientOne error");
        return "error";
    }
}
