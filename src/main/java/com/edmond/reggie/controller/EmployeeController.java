package com.edmond.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edmond.reggie.common.R;
import com.edmond.reggie.entity.Employee;
import com.edmond.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

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
}
