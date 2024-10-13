[Xinkun Blog](https://xuxinkun.github.io/)

- [Home](https://xuxinkun.github.io/)
- [About](https://xuxinkun.github.io/about/)
- [Tags](https://xuxinkun.github.io/tags/)



[grafana](https://xuxinkun.github.io/tags/#grafana)

# 使用grafana provisioning通过配置方式添加datasource和dashboard

## Use grafana provisioning to add datasources and dashboards.

Posted by XuXinkun on November 27, 2018

# grafana provisioning

grafana provisioning (http://docs.grafana.org/administration/provisioning/#provisioning-grafana)是grafana 5.0后引入的功能，用以支持通过配置的方式进行datasource和dashboard的配置。

要开启该功能，首先要在grafana的配置中增加provisioning的选项(http://docs.grafana.org/installation/configuration/#provisioning)。 即在grafana.ini中增加

```
[paths]
# folder that contains provisioning config files that grafana will apply on startup and while running.
;provisioning = /etc/grafana/provisioning
```

而后在/etc/grafana/provisioning中增加`dashboards`和`datasources`文件夹。

```
[root@local provisioning]# ll
total 0
drwxr-xr-x 2 root grafana 25 Nov 28 03:09 dashboards
drwxr-xr-x 2 root grafana 25 Nov 28 03:09 datasources
```

# datasources

datasource只支持静态配置，即，在datasources中配置好后，grafana启动时候将会进行加载。在grafana启动后在加入该文件夹，需要重启才能生效。

datasoures文件夹下需要放置对应的datasource的yaml文件，这里以`sample.yaml`为例：

```
[root@local provisioning]# cat datasources/sample.yaml 
apiVersion: 1
deleteDatasources:
 - name: influxdb
   orgId: 1
datasources:
 - id: 17
   orgId: 1
   name: influxdb
   type: influxdb
   typeLogoUrl: ''
   access: proxy
   url: http://localhost:8086
   password: root
   user: root
   database: clustersch
   basicAuth: false
   basicAuthUser: ''
   basicAuthPassword: ''
   withCredentials: false
   isDefault: false
   jsonData:
     keepCookies: []
   secureJsonFields: {}
   version: 4
   readOnly: false
```

可以看到yaml分为三部分，`apiVersion`是固定的。`deleteDatasources`是启动时候将会首先从数据库中删除的datasource的名称。通过provisioning加载datasource无法从页面进行删除，只能在`deleteDatasources`中进行删除。 再一部分就是`datasources`，是一个列表，用以表示不同的datasource。这里以influxdb为例。其他的也类似，具体可以参考其他datasource的参数说明。

# dashboards

不同于datasource，dashboards是支持动态加载的。这里介绍一个标准样例。

```
[root@local provisioning]# cat dashboards/sample.yaml 
apiVersion: 1
providers:
 - name: 'default'
   orgId: 1
   folder: ''
   type: file
   updateIntervalSeconds: 10
   options:
     path: /tmp/grafana
```

`apiVersion`是固定字段。providers是一个列表，用来存储不同的dashboard源。这里主要介绍从本机某个路径加载dashboard。`updateIntervalSeconds`是指动态加载的刷新频率，也就是10s进行一次刷新，从`/tmp/grafana`中读取所有的dashboard配置，然后将其添加或者更新到grafana中。

在`/tmp/grafana`中，只需要将dashboard的json文件丢到里面去就可以了。grafana会自动加载。json文件就是从grafana的dashboard中导出的文件即可。注意一下相关`datasource`的配置。

```
[root@local provisioning]# ll /tmp/grafana/test.json 
-rw-r--r-- 1 root root 24126 Nov 28 03:10 /tmp/grafana/test.json
```

------

作者：[xuxinkun](https://xuxinkun.github.io/)
出处：[xinkun的博客](https://xuxinkun.github.io/)
链接：<https://xuxinkun.github.io/>
本文版权归作者所有，欢迎转载。
未经作者同意必须保留此段声明，且在文章页面明显位置给出原文连接，否则保留追究法律责任的权利。
欢迎扫描右侧二维码关注微信公众号**xinkun的博客**进行订阅。也可以通过微信公众号留言同作者进行交流。

![img](img/avatar-me.jpg)

------

- [Previous
  使用pynlpir增强jieba分词的准确度](https://xuxinkun.github.io/2018/10/07/pynlpir-jieba/)
- [Next
  docker和kubernetes中hostname的使用和常见问题](https://xuxinkun.github.io/2018/12/18/docker-k8s-hostname/)

------

##### [CATALOG](https://xuxinkun.github.io/2018/11/27/grafana-provisioning/#)

- [grafana provisioning](https://xuxinkun.github.io/2018/11/27/grafana-provisioning/#grafana-provisioning)
- [datasources](https://xuxinkun.github.io/2018/11/27/grafana-provisioning/#datasources)
- [dashboards](https://xuxinkun.github.io/2018/11/27/grafana-provisioning/#dashboards)

------

##### [FEATURED TAGS](https://xuxinkun.github.io/tags/)

[docker](https://xuxinkun.github.io/tags/#docker) [openstack](https://xuxinkun.github.io/tags/#openstack) [magnum](https://xuxinkun.github.io/tags/#magnum) [kubernetes](https://xuxinkun.github.io/tags/#kubernetes) [python](https://xuxinkun.github.io/tags/#python) [kuryr](https://xuxinkun.github.io/tags/#kuryr) [neutron](https://xuxinkun.github.io/tags/#neutron) [monitor](https://xuxinkun.github.io/tags/#monitor) [osquery](https://xuxinkun.github.io/tags/#osquery)

------

##### FRIENDS

- [我的Dockone主页](http://dockone.io/people/xuxinkun)
-  

- [我的博客园](https://www.cnblogs.com/xuxinkun/)
-  

- [王亚普的博客 | WangYapu Blog](http://www.wangyapu.com/)

- 

Copyright © Xinkun Blog 2020
Theme by [Hux](http://huangxuan.me/) | 