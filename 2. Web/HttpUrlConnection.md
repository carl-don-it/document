# [Basic proxy authentication for HTTPS URLs returns HTTP/1.0 407 Proxy Authentication Required](https://stackoverflow.com/questions/34877470/basic-proxy-authentication-for-https-urls-returns-http-1-0-407-proxy-authenticat)

## 自己写socket和报文

**自己继承一个HttpURLConnection** 

You can extend `ProxiedHttpsConnection` and handle all the low level related stuff by yourself.

The following steps need to be done to make a connection over a HTTP proxy to a https website:

Note: the communication with the proxy and http server should be in [ASCII7](https://nl.wikipedia.org/wiki/ASCII_(tekenset)).

1. Send `CONNECT stackoverflow.com:443 HTTP/1.0\r\n` to the proxy
2. Send your authentication: `Proxy-Authorization: Basic c2F5WW91SGF2ZVNlZW5UaGlzSW5UaGVDb21tZW50cw==\r\n`.
3. End the first request: `\r\n`
4. Read the response from the proxy until you see the combination "\r\n\r\n".
5. Parse the first line of the response you got from the proxy and check if it starts with `HTTP/1.0 200`.
6. Start a SSL session in place over the existing connection.
7. Send the start of a http request: `GET /questions/3304006/persistent-httpurlconnection-in-java HTTP/1.0\r\n`
8. Set the proper Host header: `Host: stackoverflow.com\r\n`
9. End the request to the http server: `\r\n`
10. Read till `\r\n` and parse first line as status message
11. Read till end of stream for request body

When we want to implement the HttpUrlConnection class, there are a few things we also need to consider:

- At the time the class is constructed, the class should store data for future connections, but NOT make it directly
- Any methods can be called in any order
- The closure of the `OutputStream` means the data transfer is done, not that the connection must finish
- Every api uses the methods in a different order
- HTTP headers are case insensitive, java maps are case sensitive.

Quickly said, there are just many pitfalls

In the class I designed, it uses boolean flags to remember if the `connect` method and the `afterPostClosure` methods are called, it also has support if `getInputStream()` is called before the `OutputStream` is closed.

This class also uses as little wrapping as possible over the streams returned by the socket, to prevent being really complex.



```java
public class ProxiedHttpsConnection extends HttpURLConnection {

    private final String proxyHost;
    private final int proxyPort;
    private static final byte[] NEWLINE = "\r\n".getBytes();//should be "ASCII7"

    private Socket socket;
    private final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, List<String>> sendheaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, List<String>> proxyheaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, List<String>> proxyreturnheaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private int statusCode;
    private String statusLine;
    private boolean isDoneWriting;

    public ProxiedHttpsConnection(URL url,
            String proxyHost, int proxyPort, String username, String password)
            throws IOException {
        super(url);
        socket = new Socket();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        String encoded = Base64.encode((username + ":" + password).getBytes())
                .replace("\r\n", "");
        proxyheaders.put("Proxy-Authorization", new ArrayList<>(Arrays.asList("Basic " + encoded)));
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        connect();
        afterWrite();
        return new FilterOutputStream(socket.getOutputStream()) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(String.valueOf(len).getBytes());
                out.write(NEWLINE);
                out.write(b, off, len);
                out.write(NEWLINE);
            }

            @Override
            public void write(byte[] b) throws IOException {
                out.write(String.valueOf(b.length).getBytes());
                out.write(NEWLINE);
                out.write(b);
                out.write(NEWLINE);
            }

            @Override
            public void write(int b) throws IOException {
                out.write(String.valueOf(1).getBytes());
                out.write(NEWLINE);
                out.write(b);
                out.write(NEWLINE);
            }

            @Override
            public void close() throws IOException {
                afterWrite();
            }

        };
    }

    private boolean afterwritten = false;

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        return socket.getInputStream();

    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        this.method = method;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        sendheaders.put(key, new ArrayList<>(Arrays.asList(value)));
    }

    @Override
    public void addRequestProperty(String key, String value) {
        sendheaders.computeIfAbsent(key, l -> new ArrayList<>()).add(value);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return headers;
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        connected = true;
        socket.setSoTimeout(getReadTimeout());
        socket.connect(new InetSocketAddress(proxyHost, proxyPort), getConnectTimeout());
        StringBuilder msg = new StringBuilder();
        msg.append("CONNECT ");
        msg.append(url.getHost());
        msg.append(':');
        msg.append(url.getPort() == -1 ? 443 : url.getPort());
        msg.append(" HTTP/1.0\r\n");
        for (Map.Entry<String, List<String>> header : proxyheaders.entrySet()) {
            for (String l : header.getValue()) {
                msg.append(header.getKey()).append(": ").append(l);
                msg.append("\r\n");
            }
        }

        msg.append("Connection: close\r\n");
        msg.append("\r\n");
        byte[] bytes;
        try {
            bytes = msg.toString().getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            bytes = msg.toString().getBytes();
        }
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
        byte reply[] = new byte[200];
        byte header[] = new byte[200];
        int replyLen = 0;
        int headerLen = 0;
        int newlinesSeen = 0;
        boolean headerDone = false;
        /* Done on first newline */
        InputStream in = socket.getInputStream();
        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from remote server");
            }
            if (i == '\n') {
                if (newlinesSeen != 0) {
                    String h = new String(header, 0, headerLen);
                    String[] split = h.split(": ");
                    if (split.length != 1) {
                        proxyreturnheaders.computeIfAbsent(split[0], l -> new ArrayList<>()).add(split[1]);
                    }
                }
                headerDone = true;
                ++newlinesSeen;
                headerLen = 0;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                } else if (headerLen < reply.length) {
                    header[headerLen++] = (byte) i;
                }
            }
        }

        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }

        // Some proxies return http/1.1, some http/1.0 even we asked for 1.0
        if (!replyStr.startsWith("HTTP/1.0 200") && !replyStr.startsWith("HTTP/1.1 200")) {
            throw new IOException("Unable to tunnel. Proxy returns \"" + replyStr + "\"");
        }
        SSLSocket s = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                .createSocket(socket, url.getHost(), url.getPort(), true);
        s.startHandshake();
        socket = s;
        msg.setLength(0);
        msg.append(method);
        msg.append(" ");
        msg.append(url.toExternalForm().split(String.valueOf(url.getPort()), -2)[1]);
        msg.append(" HTTP/1.0\r\n");
        for (Map.Entry<String, List<String>> h : sendheaders.entrySet()) {
            for (String l : h.getValue()) {
                msg.append(h.getKey()).append(": ").append(l);
                msg.append("\r\n");
            }
        }
        if (method.equals("POST") || method.equals("PUT")) {
            msg.append("Transfer-Encoding: Chunked\r\n");
        }
        msg.append("Host: ").append(url.getHost()).append("\r\n");
        msg.append("Connection: close\r\n");
        msg.append("\r\n");
        try {
            bytes = msg.toString().getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            bytes = msg.toString().getBytes();
        }
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    private void afterWrite() throws IOException {
        if (afterwritten) {
            return;
        }
        afterwritten = true;
        socket.getOutputStream().write(String.valueOf(0).getBytes());
        socket.getOutputStream().write(NEWLINE);
        socket.getOutputStream().write(NEWLINE);
        byte reply[] = new byte[200];
        byte header[] = new byte[200];
        int replyLen = 0;
        int headerLen = 0;
        int newlinesSeen = 0;
        boolean headerDone = false;
        /* Done on first newline */
        InputStream in = socket.getInputStream();
        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from remote server");
            }
            if (i == '\n') {
                if (headerDone) {
                    String h = new String(header, 0, headerLen);
                    String[] split = h.split(": ");
                    if (split.length != 1) {
                        headers.computeIfAbsent(split[0], l -> new ArrayList<>()).add(split[1]);
                    }
                }
                headerDone = true;
                ++newlinesSeen;
                headerLen = 0;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                } else if (headerLen < header.length) {
                    header[headerLen++] = (byte) i;
                }
            }
        }

        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }

        /* We asked for HTTP/1.0, so we should get that back */
        if ((!replyStr.startsWith("HTTP/1.0 200")) && !replyStr.startsWith("HTTP/1.1 200")) {
            throw new IOException("Server returns \"" + replyStr + "\"");
        }
    }

    @Override
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ProxiedHttpsConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean usingProxy() {
        return true;
    }
}
```

If you are going to use this with a kind of proxy selector, you should check the protocol of the url to see if its http or https, if its http, don't use this class, and instead attach the header manually like:

```java
httpURLConnection.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
```



## https直接设置proxy-auth无效

Unfortunately there is no simple solution for what you are trying to achieve. Your 1st code doesn't work with HTTPS because you are setting the authentication header directly. Since the client encrypts all data, the proxy server has no way of extracting any information from the request.

In fact, HTTPS and proxy servers work in opposing ways. The proxy server wants to see all the data that flows between the client and the final server and take action based on what it sees. On the other hand the HTTPS protocol encrypts all data so that no one can see the data until it reaches the final destination. The encryption algorithm is negotiated between the client and the final destination so that the proxy server can't decrypt any information, in fact it can't even know which protocol the client is using.

To use a proxy server on a HTTPS connection, the client has to establish a tunnel. To do this, it has to issue a CONNECT command directly to the proxy, for example:

```java
CONNECT www.google.com:443 HTTP/1.0
```

and send the credentials to authenticate with the proxy server.

If the connection is successful the client can send and receive data through the connection. The proxy server is completely blind to the data. The data only passes through it on its way between the client and the server.

When you execute `url.openConnection(proxy)` on a HTTP URL it returns an instance of `HttpURLConnection`, when run on a HTTPS URL like in your 2nd code it retuns an instance of `HttpsURLConnection`.

You are receiving the 407 error code because the proxy server can't extract he authentication information from the header you have sent. Looking at the exception stack we can see that the exception is thrown at `sun.net.www.protocol.http.HttpURLConnection.doTunneling()` which issues the CONNECT command to establish the HTTPS tunnel through the proxy. In the source code for `sun.net.www.protocol.http.HttpURLConnection` we can see:

```java
/* We only have a single static authenticator for now.
 * REMIND:  backwards compatibility with JDK 1.1.  Should be
 * eliminated for JDK 2.0.
 */
private static HttpAuthenticator defaultAuth;
```

So it seems that the default authenticator is the only way to provide the proxy credentials.

To do what you want, you would have to go down to the connection level and handle the HTTP protocol yourself because you have to talk to the proxy server not directly to the Google server.

# 解决办法

1. [Cannot use Proxy Authentication with Https in Java](https://bugs.openjdk.java.net/browse/JDK-8210814)

2. https://stackoverflow.com/questions/41806422/java-web-start-unable-to-tunnel-through-proxy-since-java-8-update-111

   Beside the answer of mbee one can also configure this in the `net.properties` file of the jre:

   ```
   C:\Program Files (x86)\Java\jre1.8.0_131\lib\net.properties
   ```

   Currently last line 100 need to be commented out:

   Before:

   ```java
    #jdk.http.auth.proxying.disabledSchemes=
    jdk.http.auth.tunneling.disabledSchemes=Basic
   ```

   After:

   ```java
    #jdk.http.auth.proxying.disabledSchemes=
    #jdk.http.auth.tunneling.disabledSchemes=Basic
   ```

   Note that both answers need to be repeated after a Java Update, although the Java Auto Update is deactivated with Basic Internet Proxy Authentication.

# 参考文献

https://stackoverflow.com/questions/34877470/basic-proxy-authentication-for-https-urls-returns-http-1-0-407-proxy-authenticat