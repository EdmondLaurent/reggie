package com.edmond.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.edmond.reggie.entity.Employee;

/**
 * 用户服务接口 ：使用MP 作为持久层框架
 * 在Service 接口上应继承IService的接口 并说明相应的泛型类
 */
public interface EmployeeService extends IService<Employee> {
}
