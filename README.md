# 多线程爬虫和ES数据分析练习

> 我们遇到什么困难也不要怕，微笑面对它，消除Bug的最好办法就是面对Bug，坚持！才是胜利~加油，奥利给！

## 1. 项目目标
1. 爬取[sina新闻手机版](https://sina.cn)
2. 使用数据库存储并分析
3. 迁移ES
4. 简单的搜索引擎

## 2. 笔记
算法：
- 广度优先算法

流程：

![](img/flow.png)


### Mybatis 和 MySql 相关:
> ORM（Object Relational Mapping）框架
 
 > [数据库更改编码](https://www.google.com)：
 > 1. ALTER DATABASE {$数据库名} CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci; <br>
 > 2. jdbc + ?characterEncoding=utf-8<br>
 
 > [数据库清空并重建](https://mathiasbynens.be/notes/mysql-utf8mb4)：mvn flyway:clean && mvn flyway:migrate
 
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
1 row in set (0.00 sec)
```


### ElasticSearch:
- ####Realational DB :
    - Databases
        - Tables
            - Rows
                - Columns
---
- ### Elasticsearch: 
    - Indices (Index)
        - Documents (文档)
            - Fields (字段)
---

```aidl
{
    count: 102000,
    _shards: {
    total: 1,
    successful: 1,
    skipped: 0,
    failed: 0
    }
}
```


>默认Elasticsearch 在 _source 字段存储代表文档体的JSON字符串。

##### http://localhost:9200/_cluster/health

```aidl
{
cluster_name: "elasticsearch",
status: "yellow",
timed_out: false,
number_of_nodes: 1,
number_of_data_nodes: 1,
active_primary_shards: 1,
active_shards: 1,
relocating_shards: 0,
initializing_shards: 0,
unassigned_shards: 1,
delayed_unassigned_shards: 0,
number_of_pending_tasks: 0,
number_of_in_flight_fetch: 0,
task_max_waiting_in_queue_millis: 0,
active_shards_percent_as_number: 50
}
```


