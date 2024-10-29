package com.couchbase.demo.config;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.demo.config.metadata.ClusterInfo;
import com.couchbase.demo.config.metadata.SdkInfo;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MicrometerCustomMetricsConfig {
    public static final String TAG_SDK_VERSION = "sdkVersion";
    public static final String METRIC_GAUGE_SDK_INFO = "db.couchbase.sdk.info";
    public static final String METRIC_GAUGE_KV_CONNECTIONS = "db.couchbase.kv.connection";

    /**
     * This is a workaround to force the PrometheusMeterRegistry to be initialized
     * @param meterRegistryPostProcessor
     * @param prometheusRegistry
     * @return
     */
    @Bean
    InitializingBean forcePrometheusPostProcessor(BeanPostProcessor meterRegistryPostProcessor, PrometheusMeterRegistry prometheusRegistry) {
        return () -> meterRegistryPostProcessor.postProcessAfterInitialization(prometheusRegistry, "");
    }


    /**
     * Create the ClusterInfo bean for extracting the cluster UUID as common tag for Couchbase metrics
     * Future SDK versions should include this as a common tag
     * @param properties
     * @return
     */
    @Bean
    public ClusterInfo clusterInfo(CouchbaseConfig.CouchbaseProperties properties) {
        return ClusterInfo.ClusterInfoFactory.create(properties);
    }

    /**
     * Create the SdkInfo bean for extracting the SDK version as common tag for Couchbase metrics
     * Future SDK versions should include this as a new metric
     * @param clusterInfo
     * @param environment
     * @return SdkInfo
     */
    @Bean
    public SdkInfo sdkInfo(ClusterInfo clusterInfo, ClusterEnvironment environment) {
        return SdkInfo.SdkInfoFactory.create(clusterInfo, environment);
    }

    @Bean
    public MeterFilter meterFilter(ClusterInfo clusterInfo) {
        return new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                return id.getName().startsWith("db.couchbase") ? id.withTag(Tag.of("cluster_id", clusterInfo.uuid())) : id;
            }
        };
    }


    @Bean
    public MeterBinder sdkInfoBinder(SdkInfo sdkInfo) {
        return (registry) -> Gauge.builder(METRIC_GAUGE_SDK_INFO, ()-> 1)
                .tags(TAG_SDK_VERSION, sdkInfo.version())
                .register(registry);
    }

    @Bean
    public MeterBinder metricKvConnections(Cluster cluster) {
        return (registry) -> Gauge.builder(METRIC_GAUGE_KV_CONNECTIONS, ()-> cluster.environment().ioConfig().numKvConnections())
                .register(registry);
    }

}
