提醒重要：

声明式编程会比较麻烦，毕竟要去用去学习，我觉得看个人要求。当然，了解未尝不可。若你追求干净的代码，甚至有`代码洁癖`，如上众多`if else`的重复无意义劳动无疑是你的痛点，那么本文应该能够帮到你。我并不追求。

同一个属性、bean的校验往往根据业务有多种分支，写注解不是一个好方法。不建议使用（没实战，我看不出使用的价值。）

但方法参数的校验（主要是controller）可以一用。

[【方向盘】-Bean Validation](https://blog.csdn.net/f641385712/category_10338056.html)

> javax-validation的基础系列，还没有写完。

[javax validation--参数基础校验](https://blog.csdn.net/csyuyaoxiadn/article/details/56016359)

> 别人的，有一定参考价值

[分组序列@GroupSequenceProvider、@GroupSequence控制数据校验顺序，解决多字段联合逻辑校验问题【享学Spring MVC】](https://fangshixiang.blog.csdn.net/article/details/99725482)

> 包含源码解析

[Spring方法级别数据校验：@Validated + MethodValidationPostProcessor优雅的完成数据校验动作【享学Spring】](https://fangshixiang.blog.csdn.net/article/details/97402946)

> 初步入手@Validated，和controller关系不大，这是自己控制的。