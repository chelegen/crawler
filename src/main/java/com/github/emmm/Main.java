package com.github.emmm;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.List;

public class Main {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/e:/crawler/news", USERNAME, PASSWORD);
        while (true) {

            //待处理的链接池
            //从数据库加载即将处理的链接的代码
            List<String> linkPool = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");

            if (linkPool.isEmpty()) {
                break;
            }

            // 从待处理池子中捞一个来处理
            // 处理完成后从赤字（包括数据库）中删除
            String link = linkPool.remove(linkPool.size() - 1);
            deleteFromDatabase(connection, link);

            link = makeTheLinkWork(link);

            if (link == null || isLinkProcessed(connection, link)) {
                continue;
            }

            if (isTargetData(link)) {
                Document doc = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                storeIntoDatabaseIfItIsNewsPage(doc);
                InsertLinkIntoDatabase(connection, link, "INSERT INTO LINKS_ALREADY_PROCESSED (LINK) values ?");
            }
        }
    }
//        finally {
//            Runtime.getRuntime().addShutdownHook();
//        }

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> list = new ArrayList<>();
        ResultSet resultSet = null;
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return list;
    }

    private static void deleteFromDatabase(Connection connection, String link) throws SQLException {
        InsertLinkIntoDatabase(connection, link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?");
    }

    private static void InsertLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            InsertLinkIntoDatabase(connection, href, "INSERT INTO LINKS_TO_BE_PROCESSED (LINK) values ?");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT LINK FROM LINKS_ALREADY_PROCESSED where LINK = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static String makeTheLinkWork(String link) {
        //错误链接处理
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
//        if (link.contains("\\")) {
//            //去掉词条链接的 '\' 符号
//            link = link.replaceAll("\\u005c", "");
//        }
        if (link.contains("keyword")) {
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

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
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
