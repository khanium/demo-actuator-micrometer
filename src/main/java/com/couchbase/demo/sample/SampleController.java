package com.couchbase.demo.sample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class SampleController {

    final SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }


    @GetMapping("diagnostic")
    public void generateTrafficOps() {

        service.trafficOps();

    }
}
