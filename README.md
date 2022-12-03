# 瑞吉外卖开发笔记

# 环境搭建

## 数据库环境搭建

<img src="F:\new_work_study_space\瑞吉外卖资料\# 瑞吉外卖开发笔记.assets\image-20221203085549112.png" alt="image-20221203085549112" style="zoom:50%;" />、



## maven环境搭建



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

### 后端业务代码开发

#### 用户登录第一版

用户登录的业务流程如下：

```
1、将页面提交的密码password进行md5加密处理
2、根据页面提交的用户名username查询数据库
3、如果没有查询到则返回登录失败结果
4、密码比对，如果不一致则返回登录失败结果
5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
6、登录成功，将员工id存入Session并返回登录成功结果
```

```java
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);    //  从数据库中查询到的结果
        //  3、没有查询到则返回失败的结果
        if (emp == null) {
            return R.error("登陆失败：当前用户不存在");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {     //  数据库中存储的密码是加密后的密码
            return R.error("登录失败：用户输入的密码错误");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("当前员工账户已禁用...");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);  //  将从数据库中查询到的结果进行返回
    }
```

## 后台用户退出功能

### 前端代码分析

> 前端代码分析：

在进入后台管理系统主页面时会动态加载当前登录用户的名称 

```html
            <div class="right-menu">
              <div class="avatar-wrapper">{{ userInfo.name }}</div>
              <!-- <div class="logout" @click="logout">退出</div> -->
              <img src="images/icons/btn_close@2x.png" class="outLogin" alt="退出" @click="logout" />
            </div>
```

通过 vue 的data 域进行获取

在进入index主页时 vue 加载用户登录后存储在浏览器中的用户信息

```javascript
        created() {
          const userInfo = window.localStorage.getItem('userInfo')
          if (userInfo) {
            this.userInfo = JSON.parse(userInfo)
          }
          this.closeLoading()
        },
```

从浏览器当前存储中获取用户信息（并封装在 data 域中）

**退出流程：**

1. 点击退出按钮
2. 调用发送退出请求的 API （发送用户退出的axios请求）
3. 将浏览器中的用户信息删除
4. 进行页面跳转

退出流程前端代码如下：

```
          logout() {
            logoutApi().then((res)=>{
              if(res.code === 1){
                localStorage.removeItem('userInfo')
                window.location.href = '/backend/page/login/login.html'
              }
            })
          },
```

### 退出功能代码开发

> 需求分析：

1. 后端移除存储的用户信息session
2. 返回用户成功退出的结果，进行页面跳转

```java
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //  1.清除session 中保存的用户信息
        request.getSession().removeAttribute("employee");
        //  2. 返回退出成功的结果
        return R.success("当前用户退出成功");
    }
```









# 知识内容补充   

> 1. Mybatis-Plus  @TableField(fill = FieldFill.INSERT) 注解作用 





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

