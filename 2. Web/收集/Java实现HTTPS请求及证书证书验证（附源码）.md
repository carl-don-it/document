## Java实现HTTPS请求及证书证书验证（附源码）

> **欢迎大家访问我的GitHub博客**

###### https://lunan0320.github.io/

 

先发布一个初始的版本，主要以代码为主，具体的细节将之后更新。

 

### 服务器流程环节：

1、在本机服务器上生成一个`自定义证书`，格式为`jks`格式。
2、将自定义证书添加到客户端的`信任的根证书库`中，Windows系统中可以直接win+R 输入mmc即可找到添加区域。（一定是添加到信任的根证书中）
3、服务器初始化创建SSLContext上下文类型，这个过程包括创建密钥管理库和信任库两部分，使用的是`SUN509`的套件。
4、创建一个SSLContext的对象，初始化时使用`TLSv1`协议。创建两个密钥管理库和信任库的实例，作为初始化SSLContext的参数。
5、ServerFactory创建一个`SSLServerSocket`。
6、开始接受客户端发来的请求， sslServerSocket.accept()
      1）如果没有请求就`阻塞`掉；
      2）如果接收到请求就创建一个`线程`去处理它

 
一、服务器实现代码如下：

```java
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;
 
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
 
public class HTTPSServer {
    private int port = 446;
    private boolean isServerDone = false;
     
    public static void main(String[] args){
        HTTPSServer server = new HTTPSServer();
        server.run();
    }
     
    HTTPSServer(){      
    }
     
    HTTPSServer(int port){
        this.port = port;
    }
     
    // 创建并初始化 SSLContext
    private SSLContext createSSLContext(){
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("Server.jks"),"passwd".toCharArray());
             
            //创建密钥管理器
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "passwd".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            // 创建信任库
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            //初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(km,  tm, null);
             
            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }
         
        return null;
    }
     
    // 开启服务器
    public void run(){
        SSLContext sslContext = this.createSSLContext();
         
        try{
            // 创建服务器的 socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
             
            // 创建一个服务器Socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
             
            System.out.println("SSL服务器已开启~");
            while(!isServerDone){
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                 
                // Start the server thread
                new ServerThread(sslSocket).start();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
     
    // 处理从客户端来的Socket请求
    static class ServerThread extends Thread {
        private SSLSocket sslSocket = null;
         
        ServerThread(SSLSocket sslSocket){
            this.sslSocket = sslSocket;
        }
         
        public void run(){
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
             
            try{
                // Start handshake
                sslSocket.startHandshake();
                 
                // 输出一些Session信息
                SSLSession sslSession = sslSocket.getSession();
                 
                System.out.println("SSLSession :");
                System.out.println("\tProtocol : "+sslSession.getProtocol());
                System.out.println("\tCipher suite : "+sslSession.getCipherSuite());
                 
                // 定义输入输出流
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();
                 
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                 
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    System.out.println("Inut : "+line);
                     
                    if(line.trim().isEmpty()){
                        break;
                    }
                }
                 
             
                printWriter.print("This is server");
                printWriter.flush();
                 
                sslSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135
```

 

 

### 客户端流程环节：

1、在客户都添加`服务器自定义证书`，格式为`jks`格式。
2、客户端初始化创建SSLContext上下文类型，这个过程包括创建密钥管理库和信任库两部分，使用的是`SUN509`的套件。
3、创建一个SSLContext的对象，初始化时使用`TLSv1`协议。创建两个密钥管理库和信任库的实例，作为初始化SSLContext的参数。
4、通过SocketFactory创建一个`SSLSocket`，IP地址和port均为服务器的。
5、证书验证

> sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
> 是`交换密钥套件`的过程。

在这个过程中客户端会将服务器发来的证书与本地的证书去`验证`，
如果是保存在本地的服务器证书，则验证通过，否则证书验证不通过。
6、握手过程

> sslSocket.startHandshake();

可以设置个断点进去看看，或者抓包分析一下，包括了TCP三次握手的过程。

7、连接建立完成后，即可安全交互。
 
 
二、客户端代码如下

```java
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;
 
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
 
public class HTTPSClient {
    private String host = "192.168.**.**";
    private int port = 446;
     
    public static void main(String[] args){
        HTTPSClient client = new HTTPSClient();
        client.run();
    }
     
    HTTPSClient(){      
    }
     
    HTTPSClient(String host, int port){
        this.host = host;
        this.port = port;
    }
     
    // 创建并初始化 SSLContext
    private SSLContext createSSLContext(){
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("Server.jks"),"passwd".toCharArray());
             
            // 创建一个密钥管理器
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "passphrase".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            //创建一个信任库
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            // 初始化SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(km,  tm, null);
             
            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }
         
        return null;
    }
     
    // 开启客户端程序
    public void run(){
        SSLContext sslContext = this.createSSLContext();
         
        try{
            //创建Socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
             
            // 创建一个客户端的SSLsocket
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(this.host, this.port);
             
            System.out.println("SSL 客户端开启");
            new ClientThread(sslSocket).start();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
     
    // Thread handling the socket to server
    static class ClientThread extends Thread {
        private SSLSocket sslSocket = null;
         
        ClientThread(SSLSocket sslSocket){
            this.sslSocket = sslSocket;
        }
         
        public void run(){
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
             
            try{
                // 握手过程
                sslSocket.startHandshake();
                 
                // 输出关于Session的信息
                SSLSession sslSession = sslSocket.getSession();
                 
                System.out.println("SSLSession :");
                System.out.println("\tProtocol : "+sslSession.getProtocol());
                System.out.println("\tCipher suite : "+sslSession.getCipherSuite());
                 
                // Start handling application content
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();
                 
                 //定义输入以及输出缓冲区
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                 
                printWriter.println("This is client");
                printWriter.println();
                printWriter.flush();
                 
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    if(line.trim().equals("This is Client~")){
                        break;
                    }
                }
                 
                sslSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
```