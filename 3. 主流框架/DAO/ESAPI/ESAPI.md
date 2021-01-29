官网

https://blog.csdn.net/frog4/article/details/81876462

http://blog.itpub.net/69914889/viewspace-2640798/

https://vimsky.com/examples/detail/java-method-org.owasp.esapi.ESAPI.encoder.html

```java
//防止Oracle注入  

ESAPI.encoder().encodeForSQL(new OracleCodec(),queryparam)  

//防止mysql注入  

ESAPI.encoder().encodeForSQL(new MySQLCodec(Mode.STANDARD),queryparam) //Mode.STANDARK为标准的防注入方式，mysql一般用使用的是这个方式  

//防止DB2注入  

ESAPI.encoder().encodeForSQL(new DB2Codec(),queryparam)  

 
//防止Oracle注入的方法例子，为了方便仅仅给出sql语句的拼接部分  

Codec ORACLE_CODEC = new OracleCodec();  

String query ="SELECT user_id FROM user_data WHERE user_name = ‘    " + 
    ESAPI.encoder().encodeForSQL(ORACLE_CODEC,req.getParameter(＂userID＂)) + 
    "’ and user_password = ‘  "+ESAPI.encoder().encodeForSQL(ORACLE_CODEC,req.getParameter(＂pwd＂)) + 
    "’ ";
```

