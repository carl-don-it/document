提醒重要：

声明式编程会比较麻烦，毕竟要去用去学习，我觉得看个人要求。当然，了解未尝不可。若你追求干净的代码，甚至有`代码洁癖`，如上众多`if else`的重复无意义劳动无疑是你的痛点，那么本文应该能够帮到你。我并不追求。

同一个属性、bean的校验往往根据业务有多种分支，写注解不是一个好方法。不建议使用（没实战，我看不出使用的价值。）

但方法参数的校验（主要是controller）可以一用。

[【方向盘】-Bean Validation](https://blog.csdn.net/f641385712/category_10338056.html)

[深入了解数据校验：Java Bean Validation 2.0（JSR303、JSR349、JSR380）Hibernate-Validation 6.x使用案例【享学Java】](https://blog.csdn.net/f641385712/article/details/96638596)

[深入了解数据校验（Bean Validation）：基础类打点（ValidationProvider、ConstraintDescriptor、ConstraintValidator）【享学Java】](https://blog.csdn.net/f641385712/article/details/96764829)

[深入了解数据校验（Bean Validation）：从深处去掌握@Valid的作用（级联校验）以及常用约束注解的解释说明【享学Java】](https://blog.csdn.net/f641385712/article/details/97042906)

[分组序列@GroupSequenceProvider、@GroupSequence控制数据校验顺序，解决多字段联合逻辑校验问题【享学Spring MVC】](https://fangshixiang.blog.csdn.net/article/details/99725482)

[Bean Validation完结篇：你必须关注的边边角角（约束级联、自定义约束、自定义校验器、国际化失败消息...）【享学Spring】](https://blog.csdn.net/f641385712/article/details/97968775)

> javax-validation的原生系列.

[Spring方法级别数据校验：@Validated + MethodValidationPostProcessor优雅的完成数据校验动作【享学Spring】](https://fangshixiang.blog.csdn.net/article/details/97402946)

[详述Spring对Bean Validation支持的核心API：Validator、SmartValidator、LocalValidatorFactoryBean...【享学Spring】](https://blog.csdn.net/f641385712/article/details/97270786)

> spring整合。

[@Validated和@Valid的区别？教你使用它完成Controller参数校验（含级联属性校验）以及原理分析【享学Spring】](https://blog.csdn.net/f641385712/article/details/97621783)

[让Controller支持对平铺参数执行数据校验（默认Spring MVC使用@Valid只能对JavaBean进行校验）【享学Spring】](https://blog.csdn.net/f641385712/article/details/97621755)

> spring-mvc整合。建议先看数据绑定和类型转换。

[javax validation--参数基础校验](https://blog.csdn.net/csyuyaoxiadn/article/details/56016359)

> 别人的，有一定参考价值
>
> https://blog.csdn.net/durenniu/article/details/79708028 | spring mvc 校验@NULL @notNULL等_durenniu的博客-CSDN博客
> https://blog.csdn.net/FU250/article/details/80247930 | 使用@Valid+BindingResult进行controller参数校验__再见阿郎_的博客-CSDN博客
> https://blog.csdn.net/u013815546/article/details/77248003 | 使用spring validation完成数据后端校验_下一秒升华的博客-CSDN博客

