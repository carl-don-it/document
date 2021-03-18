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