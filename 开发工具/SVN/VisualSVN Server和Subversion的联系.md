# Subversion

Subversion 就是一个自由/开源版本控制的软件，它记录你每次对数据仓库的修改，并能使你找回某一个时刻修改的版本文件。它的核心就是数据仓库，你需要把你的文件提交到数据仓库，并更新数据仓库的文件，及根据需要能通过版本找到当时的数据。

所以Subversion 只需要在服务端进行安装即可。

Subversion是一个集成服务端和客户端的“内核”，在官网上提供了很多跨平台的版本http://www.subversiondownload.com/，主要实现服务端的思路：

1、如果想要纯净版，直接搭建Subversion Server，启动后，客户端通过svn命令进行操作

2、搭建基于Apache+Subversion(也是apache出品)+WebGUI的方案，好处是全部使用http协议，然后有个漂亮的后台去实现，可以去GitHub上集成一个。

后话：我的猜测，估计自己搭建的环境，需要考虑账号体系的集成，这点暂时还没搭建，后续搭建好之后再说明。

所以，如果想要操作度更自由的方案，可以试下纯手工的去linux下搭建svn环境，而且，几乎svn的目录都是一样的，即使迁移到哪个平台，启动server时指定目录即可使用。

https://tortoisegit.org

https://tortoisesvn.net/downloads.html

# TortoiseSVN

TortoiseSVN就是版本控制的客户端，通过TortoiseSVN将文件上传，更新到数据仓库，并查看版本，下载指定版本的文件。

TrotoiseSVN是一个只能在Windows平台上使用SVN客户端，同样，内核使用的还是Subversion。

# VisualSVN

《VisualSVN Server》是一个集成的[svn](https://baike.baidu.com/item/svn)服务端工具，并且包含mmc管理工具，也是一款[svn](https://baike.baidu.com/item/svn)服务端不可多得的好工具。

VisualSVN Server是只能在Windows平台上搭建的SVN服务器，内核使用的是Subversion，做了整合：apache+subversion+WMI(实现操作界面等)。

用这个的好处是一键安装即可。但是却有很多局限性：1、apache是高度精简过的东西，想要基于apache做扩展很难，比如写一些接口供第三方使用。2、WMI操作不方便，官方提供的文档完全没有这块的说明。

# 安装

Subversion安装的版本与TortoiseSVN安装的版本号好像并没有多大的关联，不过保险起见最好版本上不要差的太远。
