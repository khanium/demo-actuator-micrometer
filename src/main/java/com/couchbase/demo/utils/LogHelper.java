package com.couchbase.demo.utils;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class LogHelper {
    public static void subscribeEventsOn(String name, MeterRegistry registry) {
        registry.config().onMeterAdded((meter) -> {
            log.info(" + {} - Meter added: {}, Tags: {}",name, meter.getId().getName(), meter.getId().getTags().stream().map(tag -> tag.getKey()+": "+tag.getValue()).collect(Collectors.joining()));
        });
        registry.config().onMeterRemoved((meter) -> {
            log.info(" + {} - Meter removed: {}, Tags: {}",name, meter.getId().getName(), meter.getId().getTags().stream().map(tag -> tag.getKey()+": "+tag.getValue()).collect(Collectors.joining()));
        });
        registry.config().onMeterRegistrationFailed((id, ex) -> {
            log.info(" + {} - Meter failed: {}, Exception: {}",name, id.getName(), ex);
        });
    }


    public static void printMeters(String title, Iterable<io.micrometer.core.instrument.Meter> meters) {
        log.info(" -- {} -- ", title);
        for (io.micrometer.core.instrument.Meter meter : meters) {
            log.info("Meter: {} , Tags: {}", meter.getId().getName(), meter.getId().getTags().stream().map(Tag::getKey).collect(Collectors.joining()));
        }
        log.info(" -- -- -- -- -- -- -- -- -- -- ");
    }

    public static void printCommonTags(Set<Tag> tags) {
        log.info(" -- Common Tags -- ");
        for (Tag tag : tags) {
            log.info("Tag: {}={}", tag.getKey(), tag.getValue());
        }
        log.info(" -- -- -- -- -- -- -- -- -- -- ");
    }

    public static void printContextBeans(ApplicationContext applicationContext) {
        log.info(" -- Application Context Beans -- ");
        for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
            System.out.println("Bean Name: "+ beanDefinitionName);
        }
        log.info(" -- -- -- -- -- -- -- -- -- -- ");
    }

    public static void printMeterRegistryBeans(ApplicationContext applicationContext){

        log.debug(" -- Meter Registry Beans -- ");
        applicationContext.getBeansOfType(MeterRegistry.class).forEach((name, bean) -> {
            log.debug(" - Meter Registry Bean Name: {}", name);
            //   subscribeEventsOn(name, bean);
        });
        log.info(" -- -- -- -- -- -- -- -- -- -- ");
    }
}
