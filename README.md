# Multi-Threaded-Crawler
[![CircleCI](https://circleci.com/gh/chelegen/crawler.svg?style=svg)](https://circleci.com/gh/chelegen/crawler)

>这是基于多线程的新闻爬虫，获取的数据搭配Elasticsearch实现一个简单的新闻搜索功能

## 多线程新闻爬虫
使用Java编写爬虫，实现对[某浪新闻站](https://sina.cn)的HTTP请求、HTML解析的功能。筛选连接循环爬取新闻站内容，并存取到MySQL数据库且支持断点续传功能。
- 使用Git进行版本迭代，小步提交PR至Github主分支，用Maven进行依赖包的管理，CircleCI进行自动化测试，在生命周期绑定CheckStyle、SpotBugs等插件保证代码的质量。
- 使用Flyway数据库迁移工具完成数据库从H2数据库到MySQL数据库的迁移，并且完成数据库初始化数据表以及添加原始数据。用到了MyBatis来映射数据和Java对象的关系。对MySQL数据库进行了索引优化，使得百万级新闻内容优化了大约2倍的查询效率提升。
- 采用多线程完成爬虫任务，提高爬取速度数倍。

## 搭配Elasticsearch实现新闻搜索引擎
从MySQL灌数据，将数据存储到Elasticsearch，通过倒排索引实现了新闻搜索。

## How to build
clone项目至本地
```git clone https://github.com/chelegen/crawler.git``` <br>
从Docker启动MySQL数据库：
- [官方文档：如何使用Docker](https://docs.docker.com/get-started/)
- [个人博客：Docker的使用](https://www.cnblogs.com/pipemm/p/12300761.html)
```
docker run --name mysql -v `pwd`/docker/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 -p 3306:3306 -d mysql:8.0.18
```
数据库初始化
- Flyway不支持自动创建数据库 (初始化的前提，必须有这个数据库)
```
mvn flyway:migrate
```
项目测试
```mvn verify```

运行项目

## 笔记

#### 流程：
![](img/flow.png)

 #### 建立索引：
```$xslt
mysql> CREATE INDEX create_at_index
     -> on NEWS (created_at);
```
explain显示了mysql如何使用索引来处理select语句以及连接表。可以帮助选择更好的索引和写出更优化的查询语句。

使用方法：就是在你的sql语句前加上 explain 即可

**使用索引：**
```$xslt
mysql> explain select id,title,created_at,modified_at from NEWS where created_at = '2020-01-03';
+----+-------------+-------+------+-----------------+-----------------+---------+-------+------+-------+
| id | select_type | table | type | possible_keys   | key             | key_len | ref   | rows | Extra |
+----+-------------+-------+------+-----------------+-----------------+---------+-------+------+-------+
|  1 | SIMPLE      | NEWS  | ref  | create_at_index | create_at_index | 4       | const | 2964 | NULL  |
+----+-------------+-------+------+-----------------+-----------------+---------+-------+------+-------+
1 row in set (0.01 sec)
```
**未使用索引：**
```$xslt
mysql> explain select id,title,created_at,modified_at from NEWS where modified_at = '2020-01-03';      
+----+-------------+-------+------+---------------+------+---------+------+--------+-------------+
| id | select_type | table | type | possible_keys | key  | key_len | ref  | rows   | Extra       |
+----+-------------+-------+------+---------------+------+---------+------+--------+-------------+
|  1 | SIMPLE      | NEWS  | ALL  | NULL          | NULL | NULL    | NULL | 766827 | Using where |
+----+-------------+-------+------+---------------+------+---------+------+--------+-------------+
1 row in set (15.36 sec)
```


传统数据库和ElasticSearch的区别:
- Realational DB :
    - Databases
        - Tables
            - Rows
                - Columns
- Elasticsearch: 
    - Indices (Index)
        - Documents (文档)
            - Fields (字段)

