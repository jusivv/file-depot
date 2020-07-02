# 文件服务器使用

## Concrete 框架

### 概述

#### 上传过程

- 在用户登录后，可由程序给予当前会话文件上传的权限
- 前端（用户）在上传文件时，向后端索取token（Concrete Token Id），前端使用token向文件服务器上传文件
- 文件服务器收到前端文件上传请求后，询问后端（Concrete），验证token，如有权上传文件，则接收前端上传文件并保存

#### 下载过程

- Concrete框架支持在用户会话中缓存允许访问的文件ID及上传权限
- 后端将文件ID、token（Concrete Token Id）传递给前端，前端访问文件服务器获取文件
- 文件服务器收到前端请求后，询问后端（Concrete）验证用户token和文件ID是否匹配，如匹配允许前端访问文件

### 依赖

- 增加对concrete-core-spring、concrete-attachments-jaxrs（传递依赖了concrete-attachments）的依赖
- 将 org.coodex.concrete.attachments.client.ClientServiceImpl 类注册为REST服务
- 注入 org.coodex.concrete.spring.aspects.ConcreteAOPChain 类（如已引入org.coodex.concrete.spring.ConcreteSpringConfiguration则自动注入），并将org.coodex.concrete.attachments.clientAttachmentInterceptor加入ConcreteAOPChain

### 文件服务器配置

- 文件服务器为新增加的 Concrete 后端服务分配clientId
- 在“config.properties"中，配置访问控制器、文件访问范围、以及 Concrete 后端服务地址

```properties
# 访问控制器为concrete，0.4.0及以上版本为concrete_v0.4.0，文件服务将使用concrete认证方式
access.controller.<clientId>=concrete
# 文件访问范围，可用“,”隔开clientId授予访问该用户文件的权限，*表示所有用户上传的文件，不设置表示只能访问自己上传的文件
access.scope.<clientId>=*
# Concrete后端服务地址
access.controller.concrete.location.<clientId>=http://127.0.0.1:8090
```

### 文件上传

- 用户登录成功后，给予上传权限，执行方法“ClientServiceImpl.allowWrite()”
- 前端上传文件的URL为：http(s)://\<fileserver\>/attachments/upload/byform/{clientId}/{tokenId}/{encrypt}
  - clientId：文件服务器为 Concrete 后端服务分配的 clientId
  - tokenId：Concrete 后端服务给前端用户的会话 Id
  - encrypt：文件存储时是否需要加密，0 为不加密，1 为加密

### 文件下载

- 给包含文件 ID 的 VO 属性上增加注解“org.coodex.concrete.attachments.Attachment”，在将 VO 呈现给前端时，拦截器会将加注解的属性值加入token，给予前端访问权限
- 也可以用“ClientServiceImpl.allow(String ...)“方法，手动授权文件访问
- 前端获取（下载）文件的URL为：http(s)://\<fileserver\>/attachments/download/{fileId};c={clientId};t={tokenId}
  - clientId：文件服务器为 Concrete 后端服务分配的 clientId
  - tokenId：Concrete 后端服务给前端用户的会话 Id
  - fileId：要访问的文件 ID
