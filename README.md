## 简介
本文通过创建一个简单是实例，展示spring cloud的应用场景，使用eureka做服务注册和发现，即管理其他服务的是系统的核心，在服务宕机后能给其他服务使用新的服务，不会造成系统无法使用。使用zuul做路由转发和负载均衡，外部访问统一走网关。内部调用使用feign来注解服务，简化调用代码编写。整个spring cloud的微服务的应用雏形就是这样，上手还是非常简单的。

github地址 [spring-cloud-simple](https://github.com/qiushijie/spring-cloud-simple.git)

## 新建项目

### gradle父项目
选择gradle，然后勾选java
![](https://user-gold-cdn.xitu.io/2019/4/2/169dde71e92ace1c?w=1166&h=614&f=png&s=110524)

然后一路next

### gradle.properties
如果根目录没有gradle.properties，则新建一个，添加公共版本号
```
springboot_version=2.1.3.RELEASE
springcloud_version=Greenwich.SR1
```

### build.gradle

在跟目录的build.gradle中加入内容

```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.springframework.boot:spring-boot-gradle-plugin:${springboot_version}"
    }
}

allprojects {
    apply plugin: "java"
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    sourceCompatibility = 1.8
    targetCompatibility = 1.8

    repositories {
        repositories {
            mavenCentral()
        }
    }

    dependencyManagement {
        imports {
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springcloud_version}"
        }
    }
}
```

### 子项目
点击项目名称 -> new

![](https://user-gold-cdn.xitu.io/2019/4/2/169ddff33ccc1b51?w=1450&h=276&f=png&s=121592)

## eureka-server
eureka负责服务发现和服务注册，是spring cloud的核心，新建一个eureka-server子项目，在子目录的build.gradle中加入依赖

### 依赖

```
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
}
```

在src/main/java/包中添加Application

### Application

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```
在src/main/resources添加配置application.yml
```yml
spring:
  application:
    name: eureka-server

server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

### 启动
点击idea中运行main，浏览器访问http://localhost:8761即可看到页面

## hello-service
hello-service是一个eureka client，它把自己的服务注册到eurake中，其他client可以从服务中心获取到其他client

### 依赖
```
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```

## Application
```java
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class HelloServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloServiceApplication.class, args);
    }

    @Value("${server.port}")
    private int port;

    @RequestMapping("/hi")
    public String hello() {
        return "hi, my port=" + port;
    }

}
```

### 配置

```yml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8763
spring:
  application:
    name: hello-service
```

### 启动
访问htto://localhost:8763/hi即可看到页面

## api-gateway
api网关负责路由转发和负载均衡，转发特定路由到特定的服务中，由于其他服务都是通过特定的端口来暴露服务，网关负责把路由转发到特定的端口。

### 依赖
```
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-zuul'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```

### Application
```
@EnableDiscoveryClient
@EnableZuulProxy
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
```

### 配置
```yml
server:
  port: 8769
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
spring:
  application:
    name: zuul
zuul:
  routes:
    hello:
      path: /hello/**
      serviceId: hello-service
```

### 启动
访问http://localhost:8769/hello/hi可看到hello-service返回的页面

## call-service
服务内部相互调用，直接使用feign注解service访问，feign提供了负载均衡等。

### 依赖
```
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
}
```

### Application
```
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@RestController
public class CallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallServiceApplication.class, args);
    }

    @Autowired
    private HelloService helloService;

    @RequestMapping("/hi")
    public String hello() {
        return helloService.sayHiFromClientOne();
    }
}
```

### HelloService
```
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "hello-service")
public interface HelloService {

    @RequestMapping(value = "/hi", method = RequestMethod.GET)
    String sayHiFromClientOne();

}
```

### 配置
```
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8764
spring:
  application:
    name: call-service
```
修改api-gateway配置加入call路由到routes
```yml
  routes:
    hello:
      path: /hello/**
      serviceId: hello-service
    call:
      path: /call/**
      serviceId: call-service
```

### 启动
启动call-service，重启api-gateway，访问http://localhost:8769/call/hi，可看到由call调用hello返回的页面

## 打包

进入子项目，运行
```
../gradlew clean build -x test
```
打好的jar包在./build/libs/中