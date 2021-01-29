1. 如何判断以减少io操作，拖到内存比较
2. 收集数据前判断网页的数据结构是否有所改变，然后再收集数据。
3. 控制爬虫的时间。
4. crawl-server也可以设置一旦与crawl-admin失去联系，立马停止所有的爬虫动作



### 按页续爬不断

按天数增量爬页数的流程：有keyword，从from-end，from没有就初始化为三个月前,end没有就初始化为今天
开始：

1. start_page为null，初始化为1，表示从今天从第一页开始，end更新为今天
2. start_page>1 续爬，
3. 成功爬完，from更新为end，下次增量，start_page=1