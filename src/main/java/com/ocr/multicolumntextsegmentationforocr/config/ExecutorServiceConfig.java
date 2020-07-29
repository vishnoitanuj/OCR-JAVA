package com.ocr.multicolumntextsegmentationforocr.config;

import com.ocr.multicolumntextsegmentationforocr.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ExecutorService singleThreadService;
    private ExecutorService fixedThreadPoolService;

    @Bean("fixedThreadPool")
    public ExecutorService getFixedThreadPoolService(){
        logger.info("Initializing executor Service with pool size = {}", Constants.THREAD_POOL_SIZE);
        fixedThreadPoolService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
        return fixedThreadPoolService;
    }

    @Bean("singleThread")
    public ExecutorService getSingleThreadService(){
        logger.info("Initializing single thread executor service");
        singleThreadService = Executors.newSingleThreadExecutor();
        return singleThreadService;
    }

    @PreDestroy
    public void shutdown(){
        logger.warn("Shutting down executor Service");
        fixedThreadPoolService.shutdown();
        singleThreadService.shutdown();
    }
}
