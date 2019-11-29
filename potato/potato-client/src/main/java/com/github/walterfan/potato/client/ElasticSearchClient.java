package com.github.walterfan.potato.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ElasticSearchClient implements Closeable {
    private RestHighLevelClient client ;

    public ElasticSearchClient(HttpHost... hosts) {
        client = new RestHighLevelClient(RestClient.builder(hosts));
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            log.error("ElasticSearchClient close error", e);
        }
    }

    //create index

    //create document

    //get documents
}
