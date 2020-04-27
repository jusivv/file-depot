# File Depot

A simple security file upload/download service

## concept

- file-depot
- file-client
- user-browser

## upload

### upload by user-browser

- upload url: http(s)://<fileserver>/attachments/upload/byform/{clientId}/{tokenId}/{encrypt}
- clientId: file-client id
- tokenId: user token in file-client
- encrypt: 0 - save origin file on file-depot; 1 - save file with AES encrypt on file-depot

### upload by file-client

- upload url: http(s)://<fileserver>/attachments/upload/byform/{clientId}/{tokenId}/{encrypt}
- clientId: file-client id
- tokenId: Time-based One Time Password
- encrypt: 0 - save origin file on file-depot; 1 - save file with AES encrypt on file-depot

## download

### download by user-browser

- download url: http(s)://<fileserver>/attachments/download/{fileId};c={clientId};t={tokenId}
- clientId: file-client id
- tokenId: user token in file-client
- fileId: id of access file

### download by file-client

- download url: http(s)://<fileserver>/attachments/download/{fileId};c={clientId};t={tokenId}
- clientId: file-client id
- tokenId: Time-based One Time Password, HmacSHA1 value of fileId as secret key
- fileId: id of access file

### multi-file download

- the same as download by file-client
- fileId split with ","

## access control

### Concrete

- see filedepot.concrete.manual.md

### TOTP

- TOTP used for file-client
- AllowableAccessController、ReadOnlyAccessController based on TOTP

### Common for user-browser

- TODO
