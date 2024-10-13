# [RMI Server vs. RMI Registry](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry)

[Ask Question](https://stackoverflow.com/questions/ask)

Asked 9 years ago

Modified [9 years ago](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry?lastactivity)

Viewed 7k times



3



On [Oracle's FAQ page](http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/faq.html#domain) about Java RMI, it says:

> While the Java Remote Method Invocation (Java RMI) server can theoretically be on any host, it is usually the same host as that on which the registry is running, and on a different port. Even if the server is mistaken about its hostname or IP address (or has a hostname that simply isn't resolvable by clients), it will still export all of its objects using that mistaken hostname, but you will see an exception every time you try to receive one of those objects.

I don't understand the difference between the RMI Server and the RMI Registry. I thought they were the same thing. How can the RMI Registry work if it wasn't a server of some sorts?

- [java](https://stackoverflow.com/questions/tagged/java)
- [rmi](https://stackoverflow.com/questions/tagged/rmi)
- [distributed-computing](https://stackoverflow.com/questions/tagged/distributed-computing)

[Share](https://stackoverflow.com/q/32913180/21534310)

[Edit](https://stackoverflow.com/posts/32913180/edit)

Follow



asked Oct 2, 2015 at 18:03

![CodyBugstein's user avatar](img/SNvIc.jpg)

CodyBugstein

**23.1k**6666 gold badges222222 silver badges380380 bronze badges

- 2

  For really understand what's the difference, you must read "Writing an RMI Server" from here - [docs.oracle.com/javase/tutorial/rmi/server.html](http://docs.oracle.com/javase/tutorial/rmi/server.html) .. You will understand that RMI server is like a front door which gives you access to objects registered in RMI registry ..

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 2, 2015 at 18:32 ](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655062_32913180)

  

- 

  objects are hosted on the RMI Registry? According to the answers below, they are hosted on RMI Servers

   

  – [CodyBugstein](https://stackoverflow.com/users/1675976/codybugstein)

   

  [CommentedOct 2, 2015 at 18:34](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655120_32913180)

- 

  Made edit to the comment.

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 2, 2015 at 18:37](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655218_32913180)

- 

  @hagrawal That is exactly back to front. It is the RMI Registry which is the 'front door'.

   

  – [user207421](https://stackoverflow.com/users/207421/user207421)

   

  [CommentedOct 2, 2015 at 20:31](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53658578_32913180)

- 

  @hagawal The client can't 'connect to the RMI server' if it is a program. There is nothing to connect to. It can only connect with the Registry. *After* it has done that, it can then retrieve remote stubs by lookup, and after that it can connect with the remote objects they represent. The 'RMI server' doesn't come into that anywhere. Unless it is a host, not a program. In neither case is it a 'front door'.

   

  – [user207421](https://stackoverflow.com/users/207421/user207421)

   

  [CommentedOct 2, 2015 at 23:28](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53662229_32913180)

[Show **5** more comments](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#)



## 3 Answers

Sorted by:

​                                              Highest score (default)                                                                   Trending (recent votes count more)                                                                   Date modified (newest first)                                                                   Date created (oldest first)                              



6





## What is RMI registry:

RMI registry is a service where remote objects are registered and registered remote objects are looked up by RMI clients. If you want your object to be remotely accessible (could be many reason like you keep on updating the logic and not feasible to ship to the implementation each time, so allow remote invocation through RMI) then register it in a RMI registry and then a RMI client will look up the remote object (using remote reference of the object) and then can invoke the methods on the remote object.

Below is [definition of registry from Oracle Javadoc](http://docs.oracle.com/javase/7/docs/api/java/rmi/registry/package-summary.html)

> A registry is a remote object that maps names to remote objects. A server registers its remote objects with the registry so that they can be looked up. When an object wants to invoke a method on a remote object, it must first lookup the remote object using its name. The registry returns to the calling object a reference to the remote object, using which a remote method can be invoked.

## What is RMI server:

RMI server is that actual server where the JVM is running and the object (remote object) is living. RMI client ultimately wants this object.

**As per your concern, yes this server (RMI server) could be different from the server where RMI registry is running.** And you could understand why! I could register object from different servers in same RMI registry and I can have that registry running on a totally different server. Please read more below for more explanation on this.

## How do Java RMI clients contact remote Java RMI servers?

For an Java RMI client to contact a remote Java RMI server, the client must first hold a reference to the server (**this is where RMI registry is coming into picture, to give you reference to the RMI server**). The Naming.lookup method call is the most common mechanism by which clients initially obtain references to remote servers.

Every remote reference contains a server hostname and port number that allow clients to locate the VM that is serving a particular remote object (**this is where RMI server is coming into picture**). Once a Java RMI client has a remote reference, the client will use the hostname and port provided in the reference to open a socket connection to the remote server.

Please do read [this](http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/faq.html#netcontact) from same Oracle FAQs.

You can very well connect with the RMI registry but you may not be able to get the remote object and that's when people report `java.net.UnknownHostException`, which means that RMI registry is able to give the reference of remote object BUT RMI server which is actually hosting the remote object or running the JVM where object is living, is not found or client is not able to connect.

**So, RMI registry and RMI server are 2 different things.**

An analogy could be that HTTP server is used to provide access to HTTP resources (hyper text documents) which are available on a server. However, typically hypertext docs will be on same physical box as HTTP server but RMI registry can provide access to reference of remote objects which are on different server (RMI server).



[Share](https://stackoverflow.com/a/32916208/21534310)

[Edit](https://stackoverflow.com/posts/32916208/edit)

Follow



[edited Oct 3, 2015 at 0:12](https://stackoverflow.com/posts/32916208/revisions)





answered Oct 2, 2015 at 21:37

![hagrawal7777's user avatar](img/a04ua.jfif)

hagrawal7777

**14.6k**55 gold badges4444 silver badges7878 bronze badges

- 

  So what is an RMI server again? A host? If so, how can it be 'on' any host? How does it magically 'export objects'? 'Register its remote objects with the registry'?

   

  – [user207421](https://stackoverflow.com/users/207421/user207421)

   

  [CommentedOct 2, 2015 at 23:51 ](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53662567_32916208)

  

- 

  @EJP I have said it all in my answer - RMI server is the one which is actually running the JVM where remote object is living. Now, it could be on the same server as RMI registry or different. The remote reference returned by RMI registry contains the "RMI server" "hostname" and port number details so that clients can invoke the methods on remote object. **It is quite evident from so many statements from Oracle FAQ doc** like - *"While the Java Remote Method Invocation (Java RMI) server can theoretically be on any host, it is usually the same host as that on which the registry is running"*

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 3, 2015 at 0:17](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53662837_32916208)

- 

  And *"Every remote reference contains a server hostname and port number that allow clients to locate the VM that is serving a particular remote object"*

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 3, 2015 at 0:18](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53662840_32916208)

- 1

  @Imray : So, you got the difference between RMI server and RMI registry ??

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 4, 2015 at 0:53](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53684057_32916208)

- 1

  @hagrawal yes I think so. Thanks for the enlightening answer and discussion

   

  – [CodyBugstein](https://stackoverflow.com/users/1675976/codybugstein)

   

  [CommentedOct 7, 2015 at 5:02](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53793361_32916208)

[Show **7** more comments](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#)





2



An RMI server is a program that hosts remote objects. An RMI registry is a separate program that binds remote object names to instances. An RMI server makes remote objects accessible to clients by registering them in the registry; clients then obtain access via that same registry. An RMI registry *is* a lowercase-'s' "server", but it is not, ordinarily, an "RMI Server".

The relationship between an RMI server and RMI registry is analogous to the relationship between a web server and a DNS server that is authoritative for it.

Perhaps Oracle's [getting started guide](http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/hello/hello-world.html) explains a bit better than does the FAQ.



[Share](https://stackoverflow.com/a/32913397/21534310)

[Edit](https://stackoverflow.com/posts/32913397/edit)

Follow



answered Oct 2, 2015 at 18:19

![John Bollinger's user avatar](https://www.gravatar.com/avatar/1182b1d5518a596d4e8cfe0567a65c4d?s=64&d=identicon&r=PG)

John Bollinger

**176k**1010 gold badges9090 silver badges175175 bronze badges

- 

  Thanks for the analogy. However, the article says: `While the Java Remote Method Invocation (Java RMI) server can theoretically be on any host, it is usually the same host as that on which the registry is running`. How does that make sense? That's like every web server being on the same host that is running DNS.

   

  – [CodyBugstein](https://stackoverflow.com/users/1675976/codybugstein)

   

  [CommentedOct 2, 2015 at 18:32](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655075_32913397)

- 1

  @Imray, As the article says, the RMI registry *can* run on any host. It doesn't have to be the same one that the RMI server runs on, and as long as there are no name clashes, one RMI registry can support multiple RMI servers, on the same or different hosts. It is largely a matter of convenience that it is so common for an RMI server to make use of an RMI registry colocated on the same host. In any event, "RMI server" and "RMI registry" both refer to *programs*, not to the host on which they run.

   

  – [John Bollinger](https://stackoverflow.com/users/2402272/john-bollinger)

   

  [CommentedOct 2, 2015 at 18:37 ](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655220_32913397)

  

- 

  What would be the point of having an RMI Registry on each RMI Server? Then a client needs to be aware of all these registries rather than just one. That would be like your home PC going through lists of DNS servers to find a website, wouldn't it?

   

  – [CodyBugstein](https://stackoverflow.com/users/1675976/codybugstein)

   

  [CommentedOct 2, 2015 at 18:44](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655437_32913397)

- 

  You are reading too much into my analogy. RMI registries are designed to be *able* to support serving clients from any and everywhere across the Internet. The usual case, however, is that a given RMI registry serves to support access from a controlled pool of known clients to a small pool of known servers. In fact, the registry with which a given remote object is registered serves in a very practical way as a namespace for the registered name. If you want a particular remote object then you need to know which registry to ask.

   

  – [John Bollinger](https://stackoverflow.com/users/2402272/john-bollinger)

   

  [CommentedOct 2, 2015 at 18:57](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655854_32913397)

- 

  What you don't need to know (before consulting the registry) is where the RMI server is.

   

  – [John Bollinger](https://stackoverflow.com/users/2402272/john-bollinger)

   

  [CommentedOct 2, 2015 at 18:58](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53655909_32913397)

[Add a comment](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#)



0



From the [RMI Specification #2.4.3](http://docs.oracle.com/javase/7/docs/platform/rmi/spec/rmi-objmodel5.html):

> RMI server functions are provided by `java.rmi.server.RemoteObject` and its subclasses, `java.rmi.server.RemoteServer` and `java.rmi.server.UnicastRemoteObject` and `java.rmi.activation.Activatable.`

I believe that the terminology is confused and unnecessary. All this means is that every remote object is a server. It is best just to think of remote objects. The term '*the* RMI server' appears exactly twice in the RMI Specification, and both times it is clearly referring to a remote object.

The Registry is a remote object as well. You acquire remote objects as return values of remote methods. The Registry solves the bootstrap problem of how do you get started.

In contrast to this, the Oracle FAQ you cited seems to be using the term 'RMI server' as a kind of program that 'hosts' remote objects: but there is no necessity for such a program to even exist. Many if not most remote objects just 'host' themselves. Describing such a program as a server is just a misuse of terminology.

> How can the RMI Registry work if it wasn't a server of some sorts?

It is a remote object.



[Share](https://stackoverflow.com/a/32914914/21534310)

[Edit](https://stackoverflow.com/posts/32914914/edit)

Follow



[edited Oct 3, 2015 at 4:23](https://stackoverflow.com/posts/32914914/revisions)





answered Oct 2, 2015 at 19:58

![user207421's user avatar](https://www.gravatar.com/avatar/5cfe5f7d64f44be04f147295f5c7b88e?s=64&d=identicon&r=PG)

user207421

**310k**4444 gold badges318318 silver badges488488 bronze badges

- 

  Oracle documentation is clear in what they are saying, they mean that you can start RMI server, which may or may not be on same host as RMI registry. They are clearly talking about 2 different thing. First line from OP's link *The hostname and port number you see in the exception trace represent the address on which the looked-up server believes it is listening.*, here "looked-up server" is the RMI server.

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 2, 2015 at 21:03 ](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53659514_32914914)

  

- 

  "RMI server" will certainly not even become in RMI specification and for the same reason why HTTP server or web server will not come in HTTP specification. I feel OP's doubt is correct on RMI server and RMI registry but your answer is suggesting that it is same.

   

  – [hagrawal7777](https://stackoverflow.com/users/4691279/hagrawal7777)

   

  [CommentedOct 2, 2015 at 21:05](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53659544_32914914)

- 

  @hagrawal The Oracle document cited by the OP, which is only an FAQ let us not forget, and therefore many grades below the RMI Specification, isn't even clear as to whether an 'RMI server' is a host or a program. My answer is that the term 'RMI server' is meaningless and best avoided, not that it is the same as anything else.

   

  – [user207421](https://stackoverflow.com/users/207421/user207421)

   

  [CommentedOct 2, 2015 at 23:29](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#comment53662238_32914914)

[Add a comment](https://stackoverflow.com/questions/32913180/rmi-server-vs-rmi-registry#)