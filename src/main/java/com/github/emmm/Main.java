package com.github.emmm;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> resault = new ArrayList<>();
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resault.add(resultSet.getString(1));
            }
        }
        return resault;
    }

    private static void deleteFromDatabase(Connection connection,String link) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?")) {
            statement.setString(1,link);
            statement.executeUpdate();
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/e:/crawler/news", "root", "root");
        List<String> linkPool = null;
        boolean processed;
        while (true) {
            //待处理的链接池
            //从数据库加载即将处理的链接的代码
             linkPool = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");
            //已经处理的链接池
            //从数据库加载已经处理的链接的代码
//            Set<String> processedLinks = new HashSet<>(loadUrlsFromDatabase(connection, "select link from LINKS_ALREADY_PROCESSED"));

            if (linkPool.isEmpty()) {
                break;
            }

            //每次处理完后，更新数据库
            // ArrayList从尾部删除更有效率

            // 从待处理池子中捞一个来处理
            // 处理完成后从赤字（包括数据库）中删除
            String link = linkPool.remove(linkPool.size() - 1);

            deleteFromDatabase(connection,link);


            //询问数据库，当前链接是否被处理过？
            processed = false;
            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT LINK FROM LINKS_ALREADY_PROCESSED where LINK = ?")) {
                statement.setString(1,link);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next() && !processed) {
                    processed = true;
                }
            }

            link = makeTheLinkWork(link);

            if (link == null || processed) {
//            if (processed) {
                continue;
            }




            if (isTargetData(link)) {
                Document doc = httpGetAndParseHtml(link);
                for (Element aTag : doc.select("a")) {
                    String href = aTag.attr("href");
//                    linkPool.add(href);
                    try (PreparedStatement statement =
                                 connection.prepareStatement("INSERT INTO LINKS_TO_BE_PROCESSED (LINK) values ?")) {
                        statement.setString(1,href);
                        statement.executeUpdate();
                    }
                }
                storeIntoDatabaseIfItIsNewsPage(doc);

                try (PreparedStatement statement =
                             connection.prepareStatement("INSERT INTO LINKS_ALREADY_PROCESSED (LINK) values ?")) {
                    statement.setString(1,link);
                    statement.executeUpdate();
                }
//                processedLinks.add(link);
            } else {
                //不处理的链接
                continue;
            }
        }
    }
        /*
        finally {
            System.out.println("Exit");
            //Runtime.getRuntime().addShutdownHook();
        }*/


    private static String makeTheLinkWork(String link) {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
//        if (link.contains("\\")) {
//            //去掉词条链接的 '\' 符号
//            link = link.replaceAll("\\u005c", "");
//        }
        if (link.contains("keyword")){
            return null;
        }
        return link;
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        Elements articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println(link);

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity = response1.getEntity();
            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);
        }
    }

    private static boolean isTargetData(String link) {
        return (isNewsPage(link) || isIndexPage(link)) && isNotLoginPage(link);
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }
}
