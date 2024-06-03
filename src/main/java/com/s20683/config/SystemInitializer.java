package com.s20683.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SystemInitializer {

    protected Logger logger = LoggerFactory.getLogger(SystemInitializer.class);

    @PostConstruct
    public void init(){
        logger.info("WMPHS System Started!");
    }
}