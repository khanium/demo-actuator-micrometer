package com.couchbase.demo.config;

import com.couchbase.client.core.cnc.Meter;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.metrics.micrometer.MicrometerMeter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;

import static com.couchbase.demo.utils.LogHelper.*;
import static java.util.Objects.nonNull;

@Slf4j
@Configuration
//@EnableConfigurationProperties({CouchbaseProperties.class})
@DependsOn({"forcePrometheusPostProcessor","clusterInfo"})
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {
    private final CouchbaseProperties properties;
    private final ApplicationContext applicationContext;

    public CouchbaseConfig(CouchbaseProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Override
    public String getConnectionString() {return properties.getConnectionString();}

    @Override
    public String getUserName() {return properties.getUsername();}

    @Override
    public String getPassword() {return properties.getPassword();}

    @Override
    public String getBucketName() {return properties.getBucketName();}


   // @Bean
    public Meter getMeter() {
        Meter meterRegistryWrapper = null;
        try {
            MeterRegistry registry = applicationContext.getBean(MeterRegistry.class);
            meterRegistryWrapper = MicrometerMeter.wrap(registry);
        } catch (Exception e) {
            log.error("Failed to create MicrometerMeter, falling back to default LoggingMeter", e);
        }
        return meterRegistryWrapper;
    }


    @Override
    protected void configureEnvironment(ClusterEnvironment.Builder builder) {
        Meter meterRegistryWrapper = getMeter();
        if (nonNull(meterRegistryWrapper)) {
            log.info(" -- Configuring Couchbase Environment with MicrometerMeter -- ");
            builder.meter(meterRegistryWrapper);
        }else {
            log.warn("MeterRegistry not found in the Application Context! Configuring Couchbase Environment with default LoggingMeter");
            printMeterRegistryBeans(applicationContext);
        }
    }

    @Data
    @Configuration
    public static class CouchbaseProperties {
        @Value("${spring.couchbase.connection-string:localhost}")
        private String connectionString;
        @Value("${spring.couchbase.bucket-name:demo}")
        private String bucketName;
        @Value("${spring.couchbase.username:Administrator}")
        private String username;
        @Value("${spring.couchbase.password:password}")
        private String password;
        //TODO setup Application Properties
        // @Value("${spring.open-telemetry.processor.endpoint:http://localhost:4317}")
        // private String openTelemetryProcessorEndpoint;
        // @Value("${service-name:demo-Opentelemetry}")
        // private String serviceName;
    }
}
