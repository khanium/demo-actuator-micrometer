package com.couchbase.demo.config.metadata;

import com.couchbase.client.java.env.ClusterEnvironment;
import lombok.Builder;

@Builder
public record SdkInfo(ClusterInfo connectedTo, String version, String language, String os , String platform) {

    public static class SdkInfoFactory {

        public static SdkInfo create(ClusterInfo clusterInfo, ClusterEnvironment environment) {
            String javaVersion = environment.clientVersion().orElse("unknown");
            String userAgent = environment.userAgent().name();
            String os = environment.userAgent().os().orElse("unknown");
            String platform = environment.userAgent().platform().orElse("unknown");
            return new SdkInfo(clusterInfo, javaVersion, userAgent, os, platform);
        }
    }
}
