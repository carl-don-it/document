# **Network Type**

**在Network Type页中选择**[虚拟机](https://baike.baidu.com/item/虚拟机)网卡的“联网类型” 

1. 选择第一项，使用[桥接](https://baike.baidu.com/item/桥接)[网卡](https://baike.baidu.com/item/网卡)（VMnet0[虚拟网卡](https://baike.baidu.com/item/虚拟网卡)），表示当前[虚拟机](https://baike.baidu.com/item/虚拟机)与[主机](https://baike.baidu.com/item/主机)（指运行VMware Workstation[软件](https://baike.baidu.com/item/软件)的计算机）在同一个[网络](https://baike.baidu.com/item/网络)中。

2. 选择第二项，使用NAT[网卡](https://baike.baidu.com/item/网卡)（VMnet8[虚拟网卡](https://baike.baidu.com/item/虚拟网卡)），表示[虚拟机](https://baike.baidu.com/item/虚拟机)通过[主机](https://baike.baidu.com/item/主机)单向访问主机及主机之外的[网络](https://baike.baidu.com/item/网络)，主机之外的[网络](https://baike.baidu.com/item/网络)中的计算机，不能访问该[虚拟机](https://baike.baidu.com/item/虚拟机)。 

3. 选择第三项，只使用本地[网络](https://baike.baidu.com/item/网络)（VMnet1[虚拟网卡](https://baike.baidu.com/item/虚拟网卡)），表示[虚拟机](https://baike.baidu.com/item/虚拟机)只能访问[主机](https://baike.baidu.com/item/主机)及所有使用VMnet1虚拟网卡的虚拟机。主机之外的网络中的计算机不能访问该[虚拟机](https://baike.baidu.com/item/虚拟机)，也不能被该虚拟机所访问。

4. 选择第四项，没有[网络](https://baike.baidu.com/item/网络)连接，表明该虚拟机与[主机](https://baike.baidu.com/item/主机)没有网络连接。 