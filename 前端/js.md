# [匿名函数](https://www.cnblogs.com/ranyonsue/p/10181035.html)

 generate an anonymous method with the eval() function.

```js
var callbackStr = "function(){alert('asdf');}";  
var callback = eval(callbackStr);  
callback();

var callbackStr = "var callback = function(){alert('asdf');}";  
eval(callbackStr);  
callback();  
```

# 字体

使用特殊字体，只要引入字体的ttf文件（ttf文件，这里不做介绍，可自行百度）。如下：

```js
@font-face {
  font-family: "fashion";
  src: url('./fashion.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
.fashion-font {
  font-family: "fashion";
}
```

参考：对爬虫没什么用[在前端工程化中使用特殊字体](https://zhuanlan.zhihu.com/p/50225582)

# 形参和实参

数量可以不相等

[深入理解js中函数中的形参与实参](https://blog.csdn.net/liwenfei123/article/details/71941367)

[JavaScript-函数的调用，无参函数和有参函数，参数的的传递](https://blog.csdn.net/Ljs_cn/article/details/52809710)

[Javascript基础知识（三）：函数参数（传参）](https://blog.csdn.net/sinat_34647836/article/details/71270266)

# this

首先必须要说的是，**this的指向在函数定义的时候是确定不了的，只有函数执行的时候才能确定this到底指向谁**，**实际上this的最终指向的是那个调用它的对象**

```js
_0x26cc3c = Function(_0x1117('7') + _0x1117('8') + '\x29\x3b')(); //focus （"return (function() {}.constructor("return this")( ));"）实质是返回window，this指window，估计似乎因为匿名函数的原因
```

[彻底理解js中this的指向，不必硬背。](https://www.cnblogs.com/pssp/p/5216085.html)