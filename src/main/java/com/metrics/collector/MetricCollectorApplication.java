package com.metrics.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MetricCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricCollectorApplication.class, args);
    }
}
