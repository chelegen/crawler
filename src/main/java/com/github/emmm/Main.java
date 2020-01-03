package com.github.emmm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 8; i++) {
            threadPool.execute(new Crawler(dao));
        }
        System.out.println("当前活动的线程数: " + ((ThreadPoolExecutor)threadPool).getActiveCount());
        threadPool.shutdown();
    }
}
