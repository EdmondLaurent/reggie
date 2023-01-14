# 瑞吉外卖开发笔记

# 环境搭建

## 数据库环境搭建

执行资源文件中的sql 脚本 `db_reggie.sql` 

在当前项目中配置以下信息：

```yaml
server:
  port: 8080
spring:
  application:
    name: reggie_take_out
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/reggie?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
      username: root
      password: 1018
mybatis-plus:
  configuration:
    #在映射实体或者属性时，将数据库中表名和字段名中的下划线去掉，按照驼峰命名法映射
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
```

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

## 员工信息分页查询

### 需求与前端代码分析

> 需求分析

```
1.前端发送 Ajax请求 并附带分页参数
2.服务端通过controller接收请求（页面提交的数据）并通过调用 service查询
3.将查询到的结果返回给前端
4.接收到后端的数据通过 ELUI 渲染到对应的表格中、
```

> 前端代码分析

用户进入首页后 main 详情页面自动跳转到 list 页面

```javascript
        created() {
          this.init()
          this.user = JSON.parse(localStorage.getItem('userInfo')).username
        },
```

在 list 员工管理页面的vue对象 加载时会调用init

```javascript
          async init () {
            const params = {
              page: this.page,
              pageSize: this.pageSize,
              name: this.input ? this.input : undefined
            }
            await getMemberList(params).then(res => {
              if (String(res.code) === '1') {
                this.tableData = res.data.records || []
                this.counts = res.data.total
              }
            }).catch(err => {
              this.$message.error('请求出错了：' + err)
            })
          },
```

首先构建分页参数；然后通过 await 调用getMemberList 方法向后端发送请求

```javascript
function getMemberList (params) {
  return $axios({
    url: '/employee/page',
    method: 'get',
    params
  })
}
```

发送的请求为：`http://localhost:8080/employee/page?page=1&pageSize=10`

通过拦截器（前端拦截器）将参数与url进行拼接

```javascript
// request拦截器
  service.interceptors.request.use(config => {

    // get请求映射params参数
    if (config.method === 'get' && config.params) {
      let url = config.url + '?';
      for (const propName of Object.keys(config.params)) {
        const value = config.params[propName];
        var part = encodeURIComponent(propName) + "=";
        if (value !== null && typeof(value) !== "undefined") {
          if (typeof value === 'object') {
            for (const key of Object.keys(value)) {
              let params = propName + '[' + key + ']';
              var subPart = encodeURIComponent(params) + "=";
              url += subPart + encodeURIComponent(value[key]) + "&";
            }
          } else {
            url += part + encodeURIComponent(value) + "&";
          }
        }
      }
      url = url.slice(0, -1);
      config.params = {};
      config.url = url;
    }
    return config
  }, error => {
      console.log(error)
      Promise.reject(error)
  })
```

### 后端业务代码开发

**在使用MP 进行分页查询之前， 需要构建MP的分页查询配置类**

流程如下：

```
1. 初始化MP拦截器
2. 将分页拦截器添加到拦截器内部
3. 返回当前拦截器
```

代码：

```java
/**
 * MP 分页配置类
 * 配置MP 分页信息
 */
@Configuration
public class MybatisPlusPageConfig {
    //  MP 的分页是通过拦截器实现的
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //  1. 初始化MP拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        //  2. 将分页拦截器添加到拦截器内部
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //  3. 返回当前拦截器
        return mybatisPlusInterceptor;
    }
}
```

分页查询的业务逻辑为：

```
1.从请求中获取分页参数
2.构建分页构造器
3.构建查询条件构造器
4.执行查询条件
```

代码如下：

```java
    /**
     * 分页查询员工，根据条件模糊查询员工信息（分页）接口
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 1. 接收参数
        log.info("用户分页查询，前端传递的参数：页码：{}，每页大小：{}，模糊查询用户名称：{}", page, pageSize, name);
        //  2. 构建分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        //  3. 构建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //  第一个参数：apache工具判断String 是否为空 第二个参数 :通过对象反射获取表中对应的column 第三个参数：要查询的参数值
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);      //  根据修改时间倒序排序
        employeeService.page(pageInfo, queryWrapper);        //  不需要用返回值接收，page 查询的结果会自动封装在 pageInfo对象中
        return R.success(pageInfo);
    }
```

## 启用|禁用员工账号

 ### 需求与前端代码分析

> 前端代码分析

禁用员工账号动态显示的功能，在员工管理页面的vue对象加载的时候，从当前浏览器的本地存储中获取当前登录对象的用户名；

```javascript
        created() {
          this.init()
            //	获取当前登录用户的用户名
          this.user = JSON.parse(localStorage.getItem('userInfo')).username
        },
```

在对应按钮标签的位置，通过 v-if 语句判断是否显示当前按钮 

```html
<el-button
              type="text"
              size="small"
              class="delBut non"
              @click="statusHandle(scope.row)"
              v-if="user === 'admin'"
            >
              {{ scope.row.status == '1' ? '禁用' : '启用' }}
            </el-button>
```

调用状态修改的方法像后端发送请求：

```javascript
          //状态修改
          statusHandle (row) {
            this.id = row.id
            this.status = row.status
            this.$confirm('确认调整该账号的状态?', '提示', {
              'confirmButtonText': '确定',
              'cancelButtonText': '取消',
              'type': 'warning'
              }).then(() => {
              enableOrDisableEmployee({ 'id': this.id, 'status': !this.status ? 1 : 0 }).then(res => {
                console.log('enableOrDisableEmployee',res)
                if (String(res.code) === '1') {
                  this.$message.success('账号状态更改成功！')
                  this.handleQuery()
                }
              }).catch(err => {
                this.$message.error('请求出错了：' + err)
              })
            })
          },
```

其中 状态 status 传递的参数为三元运算表达式 ：`'status': !this.status ? 1 : 0 `

当前用户状态为 启用 （true）时 !this.status 返回false  在请求中传递的数字为 0 （后端接收到请求之后将当前数字改为 0 即可）

当前用户状态为 禁用 （false）时 !this,status 返回 true 向后端请求的数字为 1 在数据库中将 status字段改为1 即可

发送请求的 axios 方法：

```javascript
function enableOrDisableEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}
```

### 后端业务代码开发

当用户点击 `启用|禁用` 按钮时 修改数据库中存储的status字段

1. 将接收的参数封装成Employee 对象 
2. 填充 Employee 对象属性
3. 调用 MP 更新方法更新数据库中的字段内容

当前方法接收的参数是json 字符串存储的用户信息 使用 MP `updateById` 根据 id 修改的 API 即可修改整行数据库的不同的字段信息 ； 这个方法不只是可以修改状态 也可以修改其他属性

```java
    /**
     * 更新用户status状态（启用或禁用对应的用户账号）
     *
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    public R<String> updateStatus(@RequestBody Employee employee, HttpServletRequest request) {
        //  1. 将参数封装成Employee 对象
        log.info("修改状态的用户 id：{}，修改后的用户账户状态：{}", employee.getId(), employee.getStatus());
        //  2.填充属性  （当前用户id 更改时间）
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);
        employee.setUpdateTime(LocalDateTime.now());
        //  3. 调用 MP 相关接口 更改status 字段内容
        employeeService.updateById(employee);
        return R.success("用户信息更成功...");
    }
```

## 编辑员工信息

1. 用户点击编辑按钮 跳转到公用的新增页面（add.html）
2. 前端通过请求中是否存在用户 id 判断当前页面是新增还是修改信息
3. 修改信息 ：发送Ajax请求 携带用户 id 后端查询到对应的信息后返回
4. 当用户点击"保存"按钮时 复用 修改状态的方法 更改对应的字段

### 需求与前端代码分析

> 前端代码分析

因为 编辑员工与 新增员工用的是同一个 add.html 页面 通过 请求 url 中是否存在 id 值判断当前页面的功能是新增还是修改

```javascript
        created() {
          this.id = requestUrlParam('id')
          this.actionType = this.id ? 'edit' : 'add'
          if (this.id) {
            this.init()
          }
        },
```

获取请求参数中的 id 值

```javascript
//获取url地址上面的参数
function requestUrlParam(argname){
  var url = location.href
  var arrStr = url.substring(url.indexOf("?")+1).split("&")
  for(var i =0;i<arrStr.length;i++)
  {
      var loc = arrStr[i].indexOf(argname+"=")
      if(loc!=-1){
          return arrStr[i].replace(argname+"=","").replace("?","")
      }
  }
  return ""
}

```

若当前页面的功能是修改员工信息 则发送异步请求 到后端进行用户查询与数据回显

```javascript
          async init () {
            queryEmployeeById(this.id).then(res => {
              console.log(res)
              if (String(res.code) === '1') {
                console.log(res.data)
                this.ruleForm = res.data
                this.ruleForm.sex = res.data.sex === '0' ? '女' : '男'
                // this.ruleForm.password = ''
              } else {
                this.$message.error(res.msg || '操作失败')
              }
            })
          },
```

当重新提交用户信息时，发送的请求是：

复用修改状态的后端代码

### 后端业务代码开发

1. 查询数据回显到前端页面，将数据返回给前端
2. 更改的代码服用的修改状态的代码（不用修改）

查询数据并返回 -- 后端代码

```java
    /**
     * 根据 id 查询 员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getEmpById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("未查询到当前用户信息...");
    }
```

在根据 id 查询到对应的用户之后 调用修改用户账号状态的代码（修改当前对象对应信息的不同字段）

## 公共字段自动填充

### 优化需求分析

在新增或者修改员工信息时，每次操作都需要在代码层面填充一些公用的字段 （比如：`create_time` ,`update_user` 之类的）

需求：对于这些不同数据表中的共用字段做统一处理，使用MP 提供的共用字段自动填充的功能

> 🐱‍🐉MP 公用字段自动填充介绍：

公用字段自动填充，也就是在插入或更新信息时为特定的字段赋值，使用公用字段可以避免重复代码

实现步骤：

1. 在实体类属性上新增 @TableField 注解 ，指定 自动填充的策略
2. 在自动填充策略中编写数据对象处理器，在这个处理器类中统一为公用字段赋值

❓ **现在出现的问题是 在给 `createUser` , `updateUser` 字段赋值时 当前自定义的数据对象处理器中无法获取 request 对象或者获取session , 那么如何获取 当前系统的登录用户 id 信息呢？**

✔ 使用 ThreadLocal 对象进行处理 通过自定义工具类将用户登录的id 保存在 ThreadLocal 对象中

> ThreadLocal 对象介绍

客户每次发送用户请求时 对应的服务端都会分配一个新的线程来处理，也就是说在单个用户登录后的操作都属于一个线程管理

 `ThreadLocal`  是JDK 针对单个线程提供的一个类 

我们可以将用户的id信息存储在ThreadLocal对象中，在填充数据的时候 ，通过`get()`方法从 ThreadLocal 对象中获取即可

### 代码实现

在实体类上新增 @TableField 注解，指定自动填充的方式，修改后的实体类字段

```java
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
```

将对应 controller 中填充 员工公用字段的属性的代码注释

在 `common` 包下编写 工具类 `BaseContext` 工具类实现将登录用户的 id 存储到  ThreadLocal 对象中

```java
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前登登录的用户 id
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}

```

自动填充字段的数据对象处理器，填充对应的字段信息：

```java
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增阶段填充共用字段
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("新增阶段填充共用字段...");
        log.info("当前线程为：{}",Thread.currentThread().getId());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新阶段填充公用字段
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("修改阶段填充共用字段...");
        log.info("当前线程为：{}",Thread.currentThread().getId());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
```

### TIPS

ThreadLocal 对象获取当前线程信息

* ThreadLocal 对象概念与介绍：

<img src="README.assets/image-20230101132530540.png" alt="image-20230101132530540" style="zoom:50%;" />





## 新增菜品分类



<img src="README.assets/image-20230101150510103.png" alt="image-20230101150510103" style="zoom:50%;" />















<img src="README.assets/image-20230101151624929.png" alt="image-20230101151624929" style="zoom:50%;" />











## 菜品分页查询







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

## 自定义消息转换器解JavaScript页面精度问题

在根据 id 更改用户状态时 发现 JavaScript 传递的 id 只能精确到 16 位（**当前系统中的用户id为19位**）会导致精度丢失的问题 ，从而匹配不上对应的 id 

```sql
==>  Preparing: UPDATE employee SET status=?, update_time=?, update_user=? WHERE id=?
==> Parameters: 0(Integer), 2022-12-05T20:35:50.264(LocalDateTime), 1(Long), 1599246659756494800(Long)
<==    Updates: 0
```

从而导致根据id 查询用户信息失败；无法完成状态修改

> 解决方案：自定义消息转换器，将需要返回到前端的Long 类型转换 为 String 类型

1. 新建自定义对象转换器
2. 将自定义对象转换器 引入 Web MVC 并将索引值设置为 最低 0 替代 Spring MVC 中使用的默认对象转换器

* * 新建消息转换器对象
  * 设置对象转换器底层使用 Jackson 将Java对象转换为json
  * 设置自定义消息转换器的 index索引值为 0 （最低：优先使用）将自定义的消息转换器添加到MVC框架的消息转换器集合中

消息转换器作用：

完成 Java对象与 Json 字符串之间的映射 ；将统一返回结果 R 对象转换成功 Json 字符串并相应给前端页面

自定义对象映射代码如下：

1.新建自定义消息转换器

```java
/**
 * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
 * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
 * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
 */
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                .addSerializer(Long.class, ToStringSerializer.instance)// Long 类型 在转换成Json 字符串的时候自动映射为String
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```

2. 将自定义消息转换器添加到消息转换器容器中

```java
    /**
     * 扩展消息转换器对象
     * 引入自定义的消息转换器
     * 该方法在项目初始化阶段执行
     *
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展当前的消息转换器...");
        //  1. 新建消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //  2. 设置底层的消息转换器为 自定义对象转换器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //  3. 将新增的消息转换器添加到 MVC 消息转化器容器中
        converters.add(0,messageConverter);	//	注意第一个参数是 index 使用消息转换器的优先级
    }
```





