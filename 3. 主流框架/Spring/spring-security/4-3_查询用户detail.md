# 4-3 自定义用户获取逻辑，UserDetailsService

## 自定义用户认证逻辑（都是接口，并且都是bean）

1. **处理用户信息获取逻辑**

> `UserDetailsService`

<img src="img/1570630719169.png" alt="1570630719169" style="zoom: 50%;" />

------



* **2. 处理用户校验逻辑**            

  > `UserDetails`

  <img src="img/1570630846141.png" alt="1570630846141" style="zoom: 50%;" />

  ------

  

* **3. 处理密码加密解密**          

  >   `PasswordEncoder`

<img src="img/1570630651081.png" alt="1570630651081" style="zoom:80%;" />

```java
public class BCryptUtil {

    /**
     * 每次都不一样，结果应该包含了某些信息
     */
    public static String encode(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashPass = passwordEncoder.encode(password);
        return hashPass;
    }

    /**
     * 只要hashPass是用同一个密码生成的，那一定能对上
     */
    public static boolean matches(String password, String hashPass) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean f = passwordEncoder.matches(password, hashPass);
        return f;
    }

    public static void main(String[] args) {
        System.out.println(matches("sdfsdf", encode("sdfsdf")));//true
    }
}
```

