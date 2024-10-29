package com.couchbase.demo.utils;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.demo.config.CouchbaseConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class CouchbaseClientHelper {

    private final CouchbaseConfig.CouchbaseProperties properties;

    public CouchbaseClientHelper(CouchbaseConfig.CouchbaseProperties properties) {
        this.properties = properties;
    }

    private Authenticator createAuthenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getUsername(), properties.getPassword().toCharArray());
            }
        };
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private URI createBaseUrl(String connectionString) {
        String path = connectionString.replace("couchbases://","https://").replace("couchbase://","http://");
        if(!path.startsWith("http")) {
            path = "http://" + path+":8091";
        }
        return URI.create(path);
    }

    public JsonObject getClusterInfo() {
        URI baseUrl = createBaseUrl(properties.getConnectionString());
        String endpoint = "/pools/default";
        String path = baseUrl + endpoint;
        HttpClient client = HttpClient.newBuilder().authenticator(createAuthenticator()).build();
        HttpRequest request = HttpRequest.newBuilder().GET().uri(baseUrl.resolve(endpoint)).build();
        JsonObject content = null;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(isSuccess(response.statusCode())) {
                content = JsonObject.fromJson(response.body());
            }else {
                log.error("Failed to reach to {} - {} - {}", "GET", path, response.statusCode());
            }
        } catch (Exception e) {
            log.error("Exception reaching to {} - {}","GET",path,e);
        }
        return content;
    }



}
