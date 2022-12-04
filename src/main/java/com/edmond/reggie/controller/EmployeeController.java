package com.edmond.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edmond.reggie.common.R;
import com.edmond.reggie.entity.Employee;
import com.edmond.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 员工接口
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * @param request  用户登录成，将登陆成功的结果对象通过request封装到session中
     * @param employee
     * @return
     */
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

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //  1.清除session 中保存的用户信息
        request.getSession().removeAttribute("employee");
        //  2. 返回退出成功的结果
        return R.success("当前用户退出成功");
    }

    /**
     * 新增用户
     *
     * @param request
     * @param employee
     * @return
     */
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
        //  4、 调用 save 方法接口将对象存储到数据库中
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

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
}
