## [js中的匿名函数](https://www.cnblogs.com/ranyonsue/p/10181035.html)

 generate an anonymous method with the eval() function.

```js
var callbackStr = "function(){alert('asdf');}";  
var callback = eval(callbackStr);  
callback();

var callbackStr = "var callback = function(){alert('asdf');}";  
eval(callbackStr);  
callback();  
```

