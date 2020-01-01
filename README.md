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


##


 
 
 ## 
 
 > [数据库更改编码](https://www.google.com)：ALTER DATABASE {$数据库名} CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci; <br>
 > jdbc + ?characterEncoding=utf-8<br>
 > [数据库清空并重建](https://mathiasbynens.be/notes/mysql-utf8mb4)：mvn flyway:clean && mvn flyway:migrate
