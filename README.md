# 瑞吉外卖开发笔记

# 环境搭建

## 数据库环境搭建

<img src="F:\new_work_study_space\瑞吉外卖资料\# 瑞吉外卖开发笔记.assets\image-20221203085549112.png" alt="image-20221203085549112" style="zoom:50%;" />、



# 功能开发

## 后台管理员登录功能

### 需求与前端代码分析

> 需求分析

当用户在登录页面输入正确的用户名和密码之后点击登录按钮 ，将后端查询的数据在浏览器中进行保存并进行页面跳转，将浏览器的页面跳转至后端管理系统主页

```
测试用户名 ：admin
测试密码：123456
```

用户输入的用户名与密码不正确，返回错误的结果

> 前端代码分析

```html
<el-form-item prop="username">
            <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号" maxlength="20"
              prefix-icon="iconfont icon-user" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="iconfont icon-lock" maxlength="20"
              @keyup.enter.native="handleLogin" />
          </el-form-item>
```

将用户输入的用户名和密码封装在`loginForm`中 当用户点击登录按钮时调用异步的登录方法：`handleLogin` 进行数据处理和发送 axios 请求 

```javascript
            async handleLogin() {
                this.$refs.loginForm.validate(async (valid) => {
                    if (valid) {
                        this.loading = true
                        // 1.数据验证通过 调用 loginApi 发送 axios 请求
                        let res = await loginApi(this.loginForm)
                        if (String(res.code) === '1') {
                            //  2.约定 后端返回code 1 代表登录成功|| 将后端返回的用户数据保存到浏览器中
                            localStorage.setItem('userInfo', JSON.stringify(res.data))
                            //  3.进行页面跳转
                            window.location.href = '/backend/index.html'
                        } else {
                            this.$message.error(res.msg)
                            this.loading = false
                        }
                    }
                })
            }
```

前端用于发送登录请求的方法为：

```javascript
function loginApi(data) {
  return $axios({
    'url': '/employee/login',
    'method': 'post',
    data
  })
}
```



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

