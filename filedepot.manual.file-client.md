# 文件服务器使用

## 准备

- 适用于服务端获取文件服务器中的文件
- 从文件服务器获取分配的 clientId，以及访问所需的密钥（key）

## 上传

- 需要具有文件上传权限
- 通过 HTTP 协议上传文件
- URL 地址：http(s)://\<fileserver\>/attachments/upload/byform/{clientId}/{token}/{encrypt}
- clientId：文件服务器分配给终端的 clientId
- token：令牌，使用 key 作为 secret，生成的 TOTP（6 位数字）
- encrypt：文件是否加密存储，0 - 不加密； 1 - 加密

## 下载

- 需要具有文件下载权限
- 使用 HTTP 协议下载文件
- URL地址：http(s)://\<fileserver\>/attachments/download/{fileId};c={clientId};t={tokenId}
- clientId：文件服务器分配给终端的 clientId
- token：令牌，使用 key 对 fileId 进行 HmacSHA1 计算，将得到的值作为 secret，生成的 TOTP（6 位数字）
- fileId：要访问的文件，如需访问多个文件，可用","隔开 fileId，文件将压缩成Zip格式返回
- 计算 token 的 Java 代码示例如下：

```Java
byte[] fileIdInBytes = fileId.getBytes(Charset.forName("UTF-8"));
byte[] keyInBytes = Base64.getDecoder().decode(keyBase64);
SecretKey secretKey = new SecretKeySpec(keyInBytes, "HmacSHA1");
Mac mac = Mac.getInstance("HmacSHA1");
mac.init(secretKey);
byte[] hmacInBytes = mac.doFinal(fileIdInBytes);
// hmacInBytes 作为 secret，生成 TOTP
```

## TOTP计算

- TOTP（Time-based One Time Password），即基于时间的一次性密码
- TOTP 的计算方法遵循 [RCF6238](https://tools.ietf.org/html/rfc6238)
- TOTP 生成的密码为 6 位数字，步长为 30 秒（每 30 秒生成一个密码），有效窗口为 3，即一次性密码 90 秒内有效
- Java 语言可使用依赖 com.warrenstrange:googleauth:1.2.0，或更高的兼容版本 


