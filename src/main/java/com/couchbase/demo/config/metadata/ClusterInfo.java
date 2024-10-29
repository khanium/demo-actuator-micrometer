package com.couchbase.demo.config.metadata;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.demo.config.CouchbaseConfig;
import com.couchbase.demo.utils.CouchbaseClientHelper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;

@Builder
public record ClusterInfo(String uuid,String displayName,String version) {


    @Slf4j
    public static class ClusterInfoFactory {


        private static String extractUUIDFromURI(String uri) {
            String uuid = "";
            try {
                URL url = new URL("http", "dummy_host_name", uri);
                String query =  url.getQuery();
                for (String pair : query.split("&")) {
                    int idx = pair.indexOf("=");
                    if(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8).equals("uuid")){
                        uuid = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    }
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return uuid;
        }


        public static ClusterInfo create(CouchbaseConfig.CouchbaseProperties properties) {
            ClusterInfo clusterInfo = new ClusterInfo("unknown", "unknown", "unknown");
            if(nonNull(properties)) {
                JsonObject jo = new CouchbaseClientHelper(properties).getClusterInfo();
                if(nonNull(jo)) {
                    String displayName = jo.getString("clusterName");
                    String bucketsUri = jo.getObject("buckets").getString("uri");
                    String version = jo.getArray("nodes").getObject(0).getString("version");
                    //   ClusterTopology
                    //List<Objects> bucketNames = jo.getArray("bucketNames").toList();
                    log.info("clusterName: " + displayName);
                    log.info("buckets.uri (includes cluster uuid): " + jo.getObject("buckets").getString("uri"));
                    String uuid = extractUUIDFromURI(bucketsUri);
                    log.info("clusterUUID: {}", uuid);
                    clusterInfo = new ClusterInfo(uuid, displayName, version);
                } else {
                    log.error("ClusterInfo is null");
                    System.out.println("ClusterInfo is null");
                }
            } else {
                log.error("CouchbaseProperties is null");
                System.out.println("CouchbaseProperties is null");
            }
            return clusterInfo;
        }


    }
}
