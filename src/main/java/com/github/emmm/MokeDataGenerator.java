package com.github.emmm;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MokeDataGenerator {

    private static void mockData(SqlSessionFactory sqlSessionFactory, int amount) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<News> currentNews = session.selectList("com.github.emmm.MockMapper.selectNews");
            System.out.println();
            int count = amount - currentNews.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInserted = currentNews.get(index);
                    Instant currentTime = newsToBeInserted.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInserted.setCreatedAt(currentTime);
                    newsToBeInserted.setModifiedAt(currentTime);
                    session.insert("com.github.emmm.MockMapper.insertNews", newsToBeInserted);
                }
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
            session.commit();
        }
    }

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory = null;
        InputStream inputStream = null;
        try {
            String resource = "db/mybatis/config.xml";
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mockData(sqlSessionFactory, 3000);
    }
}
