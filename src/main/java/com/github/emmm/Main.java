package com.github.emmm;

public class Main {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();
        // dao 可为🔒对象

        for (int i = 0; i < 8; i++) {
            new Crawler(dao).start();
        }
    }
}
