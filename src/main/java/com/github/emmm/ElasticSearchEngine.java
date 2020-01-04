package com.github.emmm;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ElasticSearchEngine {
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Please input a search keyword:");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String keyword = reader.readLine();

            search(keyword);
        }
    }

    private static void search(String keyword) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.217.10", 9200, "http")))) {
            SearchRequest request = new SearchRequest("news");
            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword,"title","content")));
            SearchResponse search = client.search(request, RequestOptions.DEFAULT);

            search.getHits().forEach(hit->System.out.println(hit.getSourceAsString()));
        }
    }
}
