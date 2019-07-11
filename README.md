pom.xml 引入 Spring Boot 和 Spring Cloud 相关依赖，其中 JAXB API 的依赖只针对 JDK 9 以上版本，如果你是 JDK 9 以下版本，不需要配置。

#### 项目pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.valten</groupId>
    <artifactId>mycloud</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>eureka-server</module>
        <module>config-server</module>
        <module>order</module>
    </modules>

    <!-- 引入Spring Boot依赖 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.7.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- 解决JDK 9以上版本没有JAXB API的问题 -->
        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
            <version>2.3.0</version>
        </dependency>
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
            <version>2.3.0</version>
        </dependency>
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-core</artifactId>
            <version>2.3.0</version>
        </dependency>
        <dependency>
            <groupId>javax.activation</groupId>
            <artifactId>activation</artifactId>
            <version>1.1.1</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <!-- 引入Spring Cloud依赖，管理Spring Cloud生态各个组件 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Finchley.SR2</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 创建服务注册中心

##### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mycloud</artifactId>
        <groupId>com.valten</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>eureka-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>

</project>
```

##### application.yml

```yml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

>1.server.port=8761表示设置该服务注册中心的端口号
>2.eureka.instance.hostname=localhost表示设置该服务注册中心的hostname
>3.eureka.client.register-with-eureka=false,由于我们目前创建的应用是一个服务注册中心，而不是普通的应用，默认情况下，这个应用会向注册中心（也是它自己）注册它自己，设置为false表示禁止这种默认行为
>4.eureka.client.fetch-registry=false,表示不去检索其他的服务，因为服务注册中心本身的职责就是维护服务实例，它也不需要去检索其他服务

##### 启动类

> 启动一个服务注册中心的方式很简单，就是在Spring Boot的入口类上添加一个`@EnableEurekaServer`注解，如下：

```java
package com.valten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

##### 测试

> OK，做完这一切之后，我们就可以启动这一个Spring Boot 服务，服务启动成功之后，在浏览器中输入:
>
> http://localhost:8761

#### 创建服务配置服务端

##### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mycloud</artifactId>
        <groupId>com.valten</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>config-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
    </dependencies>
</project>
```

##### application.yml

```yml
server:
  port: 8762
spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
```

##### /config

> 在rsources下新建config目录，用于存放服务提供者配置文件

##### 启动类

```java
package com.valten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

#### 创建服务提供者

##### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mycloud</artifactId>
        <groupId>com.valten</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>order</artifactId>


    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
    </dependencies>
</project>
```

##### bootstrap.yml

```yml
spring:
  application:
    name: order
  profiles:
    active: dev
  cloud:
    config:
      uri: http://localhost:8762
      fail-fast: true
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

##### order-dev.yml

> 在配置中心**/config**文件夹下新建配置文件

```yml
server:
  port: 8010
```

- bootstrap.yml（bootstrap.properties）用来程序引导时执行，应用于更加早期配置信息读取，如可以使用来配置application.yml中使用到参数等

- application.yml（application.properties) 应用程序特有配置信息，可以用来配置后续各个模块中需使用的公共参数等。

- bootstrap.yml 先于 application.yml 加载

##### 启动类

```java
package com.valten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

##### 测试

```java
package com.valten.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderHandler {

    @Value("${server.port}")
    private String port;

    @GetMapping("/index")
    public String index() {
        return "order的端口：" + port;
    }
}
```

> http://localhost:8010/order/index

#### 搭建高可用服务注册中心

##### 增加配置文件

在eureka-server服务注册中心中，修改这个工程的配置文件，进而将其启动多次。如下，我向这个工程中添加两个配置文件application-peer1.yml和application-peer2.yml：

两个配置文件的内容分别如下：
application-peer1.yml:

```yml
server:
  port: 1111
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://peer2:1112/eureka/
```

application-peer2.properties:

```yml
server:
  port: 1112
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://peer1:1111/eureka/
```

关于这两个配置文件我说如下几点：

> 1.在peer1的配置文件中，让它的service-url指向peer2，在peer2的配置文件中让它的service-url指向peer1
> 2.为了让peer1和peer2能够被正确的访问到，我们需要在`C:\Windows\System32\drivers\etc`目录下的hosts文件总添加两行配置，如下:
> 127.0.0.1 peer1
> 127.0.0.1 peer2
> 3.由于peer1和peer2互相指向对方，实际上我们构建了一个双节点的服务注册中心集群

##### 生成jar文件

##### 启动项目

生成jar文件之后，我们在命令行通过java命令来启动项目，在启动的时候我们可以设置采用不同的配置文件来启动项目，命令如下：

```shell
java -jar eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=peer1  
java -jar eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=peer2
```

这两行命令表示我们分别采用application-peer1.properties和application-peer2.properties两个配置文件来启动应用，OK，执行完这两个命令之后，我们的服务注册中心就启动了两个了

- 如果**通过`java -jar`执行java程序时报出被执行程序中缺少主清单属性。**可在父项目中要添加`spring-boot-maven-plugin`这个插件来解决。

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

##### 测试

修改order项目的配置文件，如下：

```yml
eureka:
  client:
    service-url:
#      defaultZone: http://localhost:8761/eureka/
      defaultZone: http://peer1:1111/eureka/,http://peer2:1112/eureka/
```

小伙伴们注意，我们在service-url中添加了两个注册中心地址，两个地址中间用,隔开，OK，修改一下这里就可以了，接下来我们来启动这个项目，启动成功之后我们再去刷新http://localhost:1111和http://localhost:1112 两个页面，我们会发现我的服务提供者在这两个服务注册中心都注册了

OK，至此，一个高可用的服务注册中心我们就搭建成功了

#### 服务的发现与消费

服务的发现和消费实际上是两个行为，这两个行为要由不同的对象来完成：服务的发现由Eureka客户端来完成，而服务的消费由Ribbon来完成。Ribbo是一个基于HTTP和TCP的客户端负载均衡器，当我们将Ribbon和Eureka一起使用时，Ribbon会从Eureka注册中心去获取服务端列表，然后进行轮询访问以到达负载均衡的作用，服务端是否在线这些问题则交由Eureka去维护。

##### 服务提供者

###### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mycloud</artifactId>
        <groupId>com.valten</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>provider</artifactId>


    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
</project>
```

###### application.yml

```yml
server:
  port: 8020
spring:
  application:
    name: provider-service
eureka:
  client:
    service-url:
      defaultZone: http://peer1:1111/eureka/,http://peer2:1112/eureka/
```

###### 接口

```java
package com.valten.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderHandler {

    @Value("${server.port}")
    private String port;


    @GetMapping("/hello")
    public String hello() {
        return "provider的端口是：" + port;
    }
}
```

###### 启动类

```java
package com.valten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

将provider打成jar包，然后通过下面两行命令启动两个服务提供者的实例，如下：

```shell
java -jar provider-1.0-SNAPSHOT.jar --server.port=8080  
java -jar provider-1.0-SNAPSHOT.jar --server.port=8081
```

OK,如此之后，服务提供者就准备好了，接下来我们来看看服务消费者要怎么实现。

##### 服务消费者

###### pom.xml

首先创建一个Spring Boot项目，然后添加Eureka和Ribbon依赖，pom.xml文件如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mycloud</artifactId>
        <groupId>com.valten</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>ribbon-consumer</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
        </dependency>
    </dependencies>
</project>
```

###### application.yml

```yml
spring:
  application:
    name: ribbon-consumer
server:
  port: 9000
eureka:
  client:
    service-url:
      defaultZone: http://peer1:1111/eureka
```

###### 启动类

入口类上我们需要做两件事：

**1.亮明Eureka客户端身份**  

首先在入口类上添加`@EnableEurekaClient`注解，表示该应用是一个Eureka客户端应用，这样该应用就自动具备了发现服务的能力。

**2.提供RestTemplate的Bean**  

RestTemplate可以帮助我们发起一个GET或者POST请求，这个我们在后文会详细解释，这里我们只需要提供一个RestTemplate  Bean就可以了，在提供Bean的同时，添加`@LoadBalanced`注解，表示开启客户端负载均衡。

OK，基于以上两点，我们的入口类如下：

```java
package com.valten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
public class RibbonConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RibbonConsumerApplication.class, args);
    }

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

###### 接口

创建一个Controller类，并向Controller类中注入RestTemplate对象，同时在Controller中提供一个名为`/ribbon-consumer`的接口，在该接口中，我们通过刚刚注入的restTemplate来实现对HELLO-SERVICE服务提供的/hello接口进行调用。在调用的过程中，我们的访问地址是HELLO-SERVICE，而不是一个具体的地址。OK，基于以上理解，我们的Controller如下：

```java
package com.valten.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/ribbon-consumer")
    public String hello() {
        return restTemplate.getForEntity("http://PROVIDER-SERVICE/hello", String.class).getBody();
    }
}
```

然后我们向`localhost:9000/ribbon-consumer`地址发起请求，就可以看到provider工程中`/hello`接口返回的信息，如下：

> provider的端口是：8080

> provider的端口是：8081

#### 分布式配置中心

##### application.yml

```yml
server:
  port: 8762
spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/Valten123/cloud-config.git #git仓库地址
          search-paths: config-repo #仓库路径
          username: username  #账号密码写真实的快一些我觉得，不使用也能访问有点慢
          password: password
      label: master #仓库的分支
```



##### 测试

```java
package com.valten.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderHandler {

    @Value("${server.port}")
    private String port;

    @Value("${valten}")
    private String valten;

    @Autowired
    Environment env;

    @GetMapping("/hello")
    public String hello() {
        return "order的端口：" + port;
    }

    @GetMapping("/valten")
    public String valten() {
        return this.valten;
    }

    @GetMapping("/valten2")
    public String valten2() {
        return env.getProperty("valten", "未定义");
    }
}
```

配置中心通过git clone命令将配置文件在本地保存了一份，这样可以确保在git仓库挂掉的时候我们的应用还可以继续运行，此时我们断掉网络，再访问http://localhost:8030/order/prod/master，一样还可以拿到数据，此时的数据就是从本地获取的。  

##### 安全保护

开发环境中我们的配置中心肯定是不能随随便便被人访问的，我们可以加上适当的保护机制，由于微服务是构建在Spring Boot之上，所以整合Spring Security是最方便的方式。

首先添加依赖：

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

然后在application.properties中配置用户名密码：

```yml
  spring:
    security:
       user:
         name: valten
         password: 123543
```

最后在配置中心的客户端上配置用户名和密码即可，如下：

```yml
spring:
  cloud:
    config:
      username: valten
      password: 123543
```

#### 创建服务配置客户端

##### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mycloud</artifactId>
        <groupId>com.valten</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>config-client</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <!-- 重试机制 -->
        <dependency>
            <groupId>org.springframework.retry</groupId>
            <artifactId>spring-retry</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- 动态刷新机制 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

</project>
```

##### bootstrap.yml

```yml
server:
  port: 8763
spring:
  application:
    name: config-client
  cloud:
    config:
      profile: test
      label: master
      discovery:
      # 表示开启通过服务名来访问config-server
        enabled: true     
        service-id: config-server
      # http://localost:8762/config-client/test/master 
      # 失败快速响应
      fail-fast: true     
      retry:
      # 配置重试次数，默认为6
        max-attempts: 6
      # 间隔乘数，默认1.1
        multiplier: 1.1
      # 初始重试间隔时间，默认1000ms  
        initial-interval: 1000
      # 最大间隔时间，默认2000ms  
        max-interval: 2000
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
#需要开放的接口端点
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

##### 启动类

```java
package com.valten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ConfigClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}
```

有的时候，我动态的更新了Git仓库中的配置文件，那么我如何让我的config-client能够及时感知到呢？方式很简单，首先在config-client中添加如下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

该依赖中包含了/refresh端点的实现，我们将利用这个端点来刷新配置信息。然后需要在application.properties中配置忽略权限拦截：

```yml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

- 修改Git仓库的config-client-dev.yml，
- http://localhost:8762/config-client/test/master 内容已经改变
- http://localhost:8763/hello 返回的结果仍然是修改之前的内容，这是因为有缓存
- 用Postman发送post请求http://localhost:8763/actuator/refresh
- http://localhost:8763/hello 再次发送请求，参数已经改变

