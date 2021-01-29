```mysql
-- -- 最大值或者 最小值
SELECT MAX(article) AS article FROM shop; 
SELECT MIN(price) AS article FROM shop;

-- 过滤出某个字段值最大的整条记录数据，三种方法
SELECT article, dealer, price FROM shop WHERE price=(SELECT MAX(price) FROM shop); -- 子查询
SELECT s1.article, s1.dealer, s1.price FROM shop s1 LEFT JOIN shop s2 ON s1.price < s2.price WHERE s2.article IS NULL; -- 表连接
SELECT `article`, `dealer`, price FROM shop ORDER BY price DESC LIMIT 1; -- 有缺陷，有可能不止一个最大

-- 根据某一个字段进行分组,求出每一列的最大值
SELECT article, dealer, MAX(price) AS price FROM shop GROUP BY article; -- 因为使用了分组，所以这里的dealer(非分组字段)没有意义（不应该列出这一列）
SELECT article, dealer, price FROM shop s1 WHERE price=(SELECT MAX(s2.price) FROM shop s2 WHERE s1.article = s2.article); -- 关联子查询，性能最好
SELECT s1.* FROM shop s1 LEFT JOIN shop s2 ON s1.article = s2.article  AND s1.price < s2.price  where s2.price is  NULL; -- 表连接，好象差了点，统计不足

-- 该select不符合业务要求，因为没有了同一字段的约束，只有max值得约束
SELECT article, dealer, price FROM shop WHERE price IN(SELECT MAX(price) FROM shop  GROUP BY article);
```

 https://www.cnblogs.com/qixuejia/p/3637735.html 