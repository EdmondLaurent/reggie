# 瑞吉外卖开发笔记

# 环境搭建

## 数据库环境搭建

<img src="F:\new_work_study_space\瑞吉外卖资料\# 瑞吉外卖开发笔记.assets\image-20221203085549112.png" alt="image-20221203085549112" style="zoom:50%;" />、



# 问题记录

## Git SSL 错误问题

问题描述：

出现错误：

```bash
fatal: unable to access 'https://github.com/EdmondLaurent/reggie.git/': OpenSSL SSL_read: Connection was reset, errno 10054
```

解决方法：

在命令行进行以下配置：

```bash
$ git config --global --unset-all remote.origin.proxy
```

参考：

[git - fatal: unable to access 'https://github.com/xxx': OpenSSL SSL_connect: SSL_ERROR_SYSCALL in connection to github.com:443 - Stack Overflow](https://stackoverflow.com/questions/49345357/fatal-unable-to-access-https-github-com-xxx-openssl-ssl-connect-ssl-error?rq=1)

