package com.github.emmm;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataGenerator {
    public static void main(String[] args) throws IOException {
        SqlSessionFactory sqlSessionFactory = null;
        InputStream inputStream = null;
        try {
            String resource = "db/mybatis/config.xml";
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<News> newsFromMySql = getNewsFromMySql(sqlSessionFactory);

        for (int i = 0; i < 4; i++) {
            new Thread(() -> writeSingleThread(newsFromMySql)).start();
        }
    }

    private static void writeSingleThread(List<News> newsFromMySql) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.217.10", 9200, "http")))) {
            // 单线程写入 max(i)*2000 条数据
            for (int i = 0; i < 10; i++) {
                // ElasticSearch Bulk (块操作)
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : newsFromMySql) {
                    IndexRequest request = new IndexRequest("news");
                    Map<String, Object> data = new HashMap<>();

//                    data.put("content", news.getContent().length() > 10 ? news.getContent().substring(0, 10) : news.getContent());
                    data.put("content", news.getContent());
                    data.put("url", news.getUrl());
                    data.put("title", news.getTitle());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("modifiedAt", news.getModifiedAt());

                    request.source(data, XContentType.JSON);

                    bulkRequest.add(request);
//                    IndexResponse response = client.index(request, RequestOptions.DEFAULT);
//                    System.out.println(response.status().getStatus());
                }
                BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("Current thread: " + Thread.currentThread().getName() + " finishes " + i + " : " + bulk.status().getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> getNewsFromMySql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.emmm.EsMapper.selectNews");
        }
    }
}
