package com.github.emmm;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertLinkToBeProcessed(String link) throws SQLException;
}
