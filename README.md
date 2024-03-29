# File Depot

A simple security file upload/download service

## Concept

### file-depot

- provide file upload & download service

### file-client

- a web service who store his attachments on file-depot

### user-browser

- a end user using business service provided by file-client and file upload & download service provided by file-depot

## Upload

### Upload by form

- upload url: http(s)://\<fileserver\>/attachments/upload/byform/{clientId}/{token}/{encrypt}
- clientId: file-client id
- token: file access token, session token or one-time-password 
- encrypt: 0 - save origin file on file-depot; 1 - save file with AES encrypt on file-depot
- support multi-upload
- response a array contains file information, for example:

```json
[{
    "owner": "test",
    "originName": "image.jpg",
    "extName": "jpg",
    "fileId": "test$9813ada076f04c949e19eefcd9a5c4d0",
    "storeTime": 1592979547946,
    "size": 80384,
    "hashValue": null,
    "hashAlgorithm": null,
    "contentType": "image/jpeg",
    "cipherModel": "aes.v1",
    "salt": "<salt>"
}]
```

### Upload by base64 string

- upload url: http(s)://\<fileserver\>/attachments/upload/bybase64
- method: post, content-type: application/json
- JSON content like this:

```json
{
  "clientId": "test",
  "token": "<token>",
  "fileName": "image.jpg",
  "contentType": "image/jpeg",
  "encrypt": true,
  "base64File": "<base64string>"
}
```

- response a JSON object with file information, for example:

```json
{
    "owner": "test",
    "originName": "image.jpg",
    "extName": "jpg",
    "fileId": "test$9813ada076f04c949e19eefcd9a5c4d0",
    "storeTime": 1592979547946,
    "size": 80384,
    "hashValue": null,
    "hashAlgorithm": null,
    "contentType": "image/jpeg",
    "cipherModel": "aes.v1",
    "salt": "<salt>"
}
```

## Download

### By stream

- download url: http(s)://\<fileserver\>/attachments/download/{fileId};c={clientId};t={token}
- clientId: file-client id
- token: file access token, session token or one time password
- fileId: file id which you want to download
- you can join fileId with "," to download files
- multi-file download will pack all request files in a zip file

### By base64

- download url: http(s)://\<fileserver\>/attachments/base64/{fileId};c={clientId};t={token}
- clientId: file-client id
- token: file access token, session token or one time password
- fileId: file id which you want to download, only one file to get for each request
- response a JSON like this: 

```json
{
"fileName":"5b5d2fc0-c028-11ea-a8cd-2bac079b3bed.jpg",
"contentType":"image/jpeg",
"extName":"jpg",
"base64String":"<base64String>",
"size":12641
}
```

## Access control

### TOTP based

- a TOTP as a token, apply to file-client
- TOTP is 6 digits, step size is 30 seconds, window size is 3 (expires in 1.5 minutes), see [RCF6238](https://tools.ietf.org/html/rfc6238)
- AllowableAccessController、ReadOnlyAccessController based on TOTP
- file-client & file-depot hold the same secret to calculate one time password
- for uploading, supply TOTP as token
- for downloading, secret as key, fileId as content, calculate HmacSHA1 value which as new secret to calculate the one time password

### file-client feedback

- apply to user-browser
- file-client create token for user-browser
- user-browser request file-depot with token
- file-depot authenticate token by request file-client

#### Common feedback

- request file-client by url http://\<host\>:\<port\>/\<path\>/\[ writable | readable | deletable \]
- request Content-Type is 'application/json'
- body sample:

```JSON
{
"token": "file-client-user-token"
}
```
or
```JSON
{
"token": "file-client-user-token",
"fileId": "file-id or file-ids split with ','"
}
```

#### Concrete

- see filedepot.manual.concrete.md

## Dockerize

```shell
docker build -t file-depot:1.0.0 .
docker-compose up -d
docker-compose down
```