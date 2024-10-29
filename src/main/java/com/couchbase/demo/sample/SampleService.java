package com.couchbase.demo.sample;


import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class SampleService {
private static final String DEFAULT_TEST_DOC_ID= "test:001";
    private static final String DEFAULT_QUERY = "SELECT * FROM _ WHERE meta().id = '$1'";
    private static final String DEFAULT_SLOW_QUERY = "SELECT * FROM _ WHERE meta().id LIKE '$1'";
    final Cluster cluster;
    final Collection defaultCollection;
    final String defaultQuery;
    final Random random = new Random();


    public SampleService(Cluster cluster, @Value("${spring.data.couchbase.bucket-name:demo}") String bucketName) {
        this.cluster = cluster;
        //TODO change this demo by Spring data demo CRUD entity
        this.defaultCollection = cluster.bucket(bucketName).defaultCollection();
        this.defaultQuery = DEFAULT_SLOW_QUERY.replace("_","`"+defaultCollection.bucketName()+"`.`"+defaultCollection.scopeName()+"`.`"+defaultCollection.name()+'`').replace("$1","test%");
    }


    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    public void trafficOps() {
        long maxUpserts = 50;
        long maxReads = 50;
        long maxQueries = 100;
        log.info("scheduled task: generating {} write/s, {} read/s, {} queries/s traffic", maxUpserts, maxReads, maxQueries);
         for(int i=0;i<maxUpserts;i++) {
            upsertDocTest();
        }

        for(int i=0;i<maxReads;i++) {
            getDocTest();
        }

        for(int i=0;i<maxQueries;i++) {
            queryDocTest();
        }



    }

    private JsonObject generateDocTest() {
        return JsonObject.create()
                .put("name", "test")
                .put("upsertedAt",LocalDateTime.now().toString());
    }

    private void upsertDocTest() {
        int id = random.nextInt(1_000_000);
        defaultCollection.upsert(DEFAULT_TEST_DOC_ID+"_"+id,generateDocTest()).cas();
    }

    private void getDocTest() {
        int id = random.nextInt(1_000_000);
        if(defaultCollection.exists(DEFAULT_TEST_DOC_ID+"_"+id).exists())
            defaultCollection.get(DEFAULT_TEST_DOC_ID+"_"+id).cas();
    }

    private void queryDocTest() {
       cluster.query(defaultQuery).rowsAsObject().size();
    }


}
