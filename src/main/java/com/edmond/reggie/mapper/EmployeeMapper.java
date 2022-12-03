package com.edmond.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edmond.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
