# Grafana 配置邮件告警



wyp257



于 2021-07-19 18:30:47 发布



阅读量4.3k

![img](img/tobarCollect2.png) 收藏 10

![img](img/newHeart2023Black.png)点赞数 1

分类专栏： [Grafana](https://blog.csdn.net/wyp257/category_11220255.html) 文章标签： [linux](https://so.csdn.net/so/search/s.do?q=linux&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art) [运维](https://so.csdn.net/so/search/s.do?q=运维&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art)

版权

[![img](img/20201014180756780.png)Grafana专栏收录该内容](https://blog.csdn.net/wyp257/category_11220255.html)

1 篇文章0 订阅

订阅专栏

## Grafana 配置邮件告警

**Grafana版本为8.0.4，以配置qq邮箱为例**

### 修改grafana配置文件

grafana的配置文件为 **/etc/grafana/grafana.ini** 或者 **/usr/share/grafana/conf/default.ini** ，如果不确定更改哪个配置文件可查看grafana服务启动使用哪个配置文件。

```bash
service grafana-server status
1
```

结果如图，其中**–config**参数所示即grafana服务的配置文件。
![grafana-server status](https://i-blog.csdnimg.cn/blog_migrate/911b9061ee0aa8e28dcde0bc9d979568.png#pic_center)
然后，修改 **/etc/grafana/grafana.ini** 文件中 **smtp** 字段下的相关内容
![grafana smtp setting](https://i-blog.csdnimg.cn/blog_migrate/e9f8f68a467dec0fb596c25dac4f7a59.png#pic_center)
其中，**host**为邮箱运营商的SMTP服务器，**user**为发件人邮箱地址（如：xxx@qq.com），**password**为发件人邮箱开启SMTP服务后生成的授权码（这里我配置成邮箱登录密码没通过测试，设置为授权码后测试通过），**from_address** 需和 **user** 保持一致。

### 开启邮箱SMTP服务

登录qq邮箱，在**设置**中选择**账户**，开启SMTP服务并获取授权码（用于设置grafana配置文件中的**password**字段）。
![qq_email_smtp](https://i-blog.csdnimg.cn/blog_migrate/b4b607b230d5dfe2590afd6ab169952c.png#pic_center)

### 配置grafana告警通道

打开网页http://**ip:port**，ip:port为grafana服务（如：http://localhost:3000）。
如果是首次登录，默认用户名和密码均为**admin**，修改登录密码，在grafana面板中配置邮件告警如下图所示
![grafana Alerting Email](https://i-blog.csdnimg.cn/blog_migrate/49427962c01d520620462ff44520a16a.png#pic_center)
测试通过后即可在需要设置告警的**Panel**中使用该配置发送告警邮件。
相关参数说明：

- **Default** 默认关闭，开启后所有**Panel**中设置的告警时都会默认使用此通道。
- **Include image** 开启后，表示发送告警图片，该功能需要安装Grafana插件[Grafana Image Renderer](https://grafana.com/grafana/plugins/grafana-image-renderer/)。
- **Disable Resolve Message** 开启后，当告警解除时不发送[OK]邮件；默认发送恢复邮件。
- **Send reminders** 开启后，需设置发送间隔，表示在告警发生后且持续处于告警状态时，每间隔一段时间发送一次邮件；否则只发送一次。

### 配置Panel告警规则

![grafana Panel Alert setting](https://i-blog.csdnimg.cn/blog_migrate/03dc21ada53fdf8ff5409b83b5c86588.png#pic_center)
相关参数说明：

- **Rule** 可编辑Name，配置触发告警的规则。在时间范围（For）中，评估每隔多长时间（Evaluate every），是否处于告警状态。如果一直满足告警条件则开始告警。
- **Conditions** 告警条件，WHEN中可设置要使用的聚合函数（如：avg()，min()，sum()等）；OF中的query(A,1m,now)字段表示对应Query中的指标A，在1分钟前到现在这段时间范围内数值超出（IS ABOVE）阈值0.95；追加条件 **+** 可选择**与|或**两种条件。
- **No data and error handling** 对无数据或错误的处理。对于查询无返回数据，可选的设置状态有Alerting，No Data，Keep Last State和Ok四种；对于查询出错或超时，可选的设置状态有Alerting和Keep Last State两种。
- **Notifications** 通知。配置使用的告警方式（如果告警通道中选择了Default，则默认选择该通道；不可去除，除非在通道中取消Default）；编辑Message，发送具体的告警邮件信息。
- **Tags** 标签。可添加标签和标签值（具体什么效果，我没试过）。



文章知识点与官方知识档案匹配，可进一步学习相关知识