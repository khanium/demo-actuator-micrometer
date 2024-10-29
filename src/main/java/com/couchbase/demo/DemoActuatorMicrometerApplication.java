package com.couchbase.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class DemoActuatorMicrometerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoActuatorMicrometerApplication.class, args);
    }

}
