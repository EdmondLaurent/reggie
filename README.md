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

### 后端业务代码开发

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

## 登录功能过滤器（权限）

### 需求分析与前端代码分析

> 需求分析

当用户未登录直接访问后台首页时，通过拦截器将用户重定向至登录页面

> 前端代码分析

在 request请求的工具包中 **所有的后端响应数据都要经过响应拦截器** 

```javascript
  service.interceptors.response.use(res => {
      if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 返回登录页面
        console.log('---/backend/page/login/login.html---')
        localStorage.removeItem('userInfo')
        window.top.location.href = '/backend/page/login/login.html'
      } else {
        return res.data
      }
    },
```

当响应的结果 为 error 代码 0 且 后端响应数据的信息 为：NOTLOGIN 时，表示当前用户未登录，跳转回到登陆页面

### 后端业务代码开发

> 业务流程分析：

```
/*
1、获取本次请求的URI
2、判断当前URI是否需要处理
3、若当前请求内容不需要处理（登录登出，静态页面）则直接放行当前请求
4、判断当前是否有用户登录的信息，有用户登录信息则直接放行
5、无用户登录信息则返回没有用户登录的信息
 */
```

通过请求过滤器重写 `doFilter` 方法，根据请求的类型和当前session中是否含有用户信息决定是否放行当前请求

过滤器的完整代码为：

```java
// 定义拦截所有请求的过滤器
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class loginCheckFilter implements Filter {

    //`Spring core 自带的工具用于检测请求是否与不要被处理的请求路径一致
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到的用户请求：{}", requestURI);
        //   2、判断当前URI是否需要处理
        //  2-1、定义不需要被处理的URI列表
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        //  2-2、判断当前请求是否需要被处理
        boolean check = checkURI(urls, requestURI);
        //  3、若当前请求内容不需要处理（登录登出，静态页面）则直接放行当前请求
        if (check){
            filterChain.doFilter(request,response);
            return;     //  放行请求，退出当前方法
        }
        //   4、判断当前是否有用户登录的信息，有用户登录信息则直接放行
        if (request.getSession().getAttribute("employee") != null){
            filterChain.doFilter(request,response);
            return;
        }
        //  5、无用户登录信息则返回没有用户登录的信息
        log.info("系统中没有用户登录的信息...");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 判断当前请求是否需要被处理，返回check旗帜
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean checkURI(String[] urls, String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                //  请求路径与不需要被处理的路径一致，不需要处理
                return true;
            }
        }
        return false;
    }
}
```

#### TIPS

* AntPathMatcher 是 Spring 核心包中提供的工具，在当前项目中的作用是：判断用户发送的请求是否与预定义的不需要处理的请求一致
* 在当前方法的返回值是 void 时 可以通过 `response.getWriter().write()` 的方法获取输出流 ，向前端返回数据；如：

```java
 response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
```

## 新增员工功能

### 需求与前端代码分析

> 需求分析

用户点击新增员工按钮 ---> 跳转到新增员工页面 ---> 填写后台用户（员工）信息表单（注意是否启用属性）

用户提交表单后在 `employee` 表中新增关于员工的数据

特殊字段： 

username : 有唯一约束，用户通过用户名进行登录

status：当前账户是否启用，0表示禁用 1表示启用

> 前端代码分析

当用户前往index.html 主页的时候 在右侧的内容详情页 默认前往的就是员工管理详情页面

```html
          <div class="app-main" v-loading="loading">
            <div class="divTmp" v-show="loading"></div>
            <iframe
              id="cIframe"
              class="c_iframe"
              name="cIframe"
              :src="iframeUrl"	<---------  这个属性表示当前嵌入的页面URL
              width="100%"
              height="auto"
              frameborder="0"
              v-show="!loading"
            ></iframe>
          </div>
        </div>
      </div>
```

iframeUrl 在 index 页面 vue 对象的 data 域中 默认属性是 员工管理详情页

```javascript
iframeUrl: 'page/member/list.html',
```

用户点击其他页面的链接时 变更这个属性的 URL 值即可

在员工管理界面 当用户点击 `添加员工` 按钮之后 触发页面跳转 

主页面会显示添加员工的表单信息：

```javascript
           // 添加
          addMemberHandle (st) {
            if (st === 'add'){
              //  调用 index 页面的切换详情页方法 将参数中的 url 赋值给详情页属性url
              //  目的是在主界面切换对应的显示内容
              window.parent.menuHandle({
                id: '2',
                url: '/backend/page/member/add.html',
                name: '添加员工'
              },true)
            } else {
              window.parent.menuHandle({
                id: '2',
                url: '/backend/page/member/add.html?id='+st,
                name: '修改员工'
              },true)
            }
          },
```

切换详情页面的方法：

```javascript
          menuHandle(item, goBackFlag) {
            this.loading = true
            this.menuActived = item.id
            this.iframeUrl = item.url   <------ 将参数值中的 url 复制给当前vue对象data域中的url属性达到切换页面的效果
            this.headTitle = item.name
            this.goBackFlag = goBackFlag
            this.closeLoading()
          },
```

### 后端业务代码开发

> 需求分析

```
1、接收到请求参数 json 字符串（转为Employee对象）
2、设置默认密码
3、填充其他属性
4、调用 save 方法接口将对象存储到数据库中
```

代码详情如下：

```java
    @PostMapping
    public R<String> saveUser(HttpServletRequest request, @RequestBody Employee employee) {
        //  1、接收参数 -- 转换成 Employee对象
        log.info("新增用户；接收的参数：{}", employee);
        // 2、设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        // 3、填充其他属性
        employee.setCreateTime(LocalDateTime.now());    //  LocalDateTime.now() 获取当前系统时间
        employee.setUpdateTime(LocalDateTime.now());
        //  在session中获取当前登录用户的id
        Long employeeId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(employeeId);
        employee.setUpdateUser(employeeId);
        //  4、调用 save 方法接口将对象存储到数据库中
        employeeService.save(employee);
        return R.success("新增员工成功");
    }
```

#### TIPS

* LocalDateTime.now() 获取当前系统时间
* DigestUtils.md5DigestAsHex()  Sprnig core 中提供的工具类；作用是将密码进行MD5加密；参数是byte数组

### 异常处理器业务要求与开发

由于设置了username字段的唯一约束；当新增的员工账号（username）字段发生重复时，会出现如下错误：

```java
 Duplicate entry 'zhangsan' for key 'idx_username'; nested exception is java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'] with root cause
```

所以需要对错误进行捕获和处理；

**新增全局异常处理器；（通过代理的方式处理所有请求）；用于捕获和处理程序在运行过程中出现的所有异常**

```
1、若抛出用户名重复的异常，在异常信息中获取重复的用户名字段
2、若不是因为这个异常则抛出未知的错误
```

代码详情如下：

```java
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//  所有在类名上含有 Controller 和 RestController 的方法都会被捕获
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理异常的方法（通过反射获取异常信息）
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        //  1、若抛出用户名重复的异常，在异常信息中获取重复的用户名字段
        if (exception.getMessage().contains("Duplicate entry")) {
            String[] split = exception.getMessage().split(" ");
            String exMsg = "当前系统中已存在用户：" + split[2];
            return R.error(exMsg);
        }
        //  2、若不是因为这个异常则抛出未知的错误
        return R.error("运行时出现了未知的错误...");
    }

}

```

#### TIPS

* @ControllerAdvice(annotations = {RestController.class, Controller.class}) 代表拦截所有类上带有 @Controller 和 @RestController的注解 的网络请求，参数是对应Controller 的class 组成的数组

* @ExceptionHandler(SQLIntegrityConstraintViolationException.class) 表示捕获指定的异常信息并进行处理









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





# 杂事儿

13912345678
