package com.diy.service;

import com.diy.dto.EmployeeDTO;
import com.diy.dto.EmployeeLoginDTO;
import com.diy.dto.EmployeePageQueryDTO;
import com.diy.entity.Employee;
import com.diy.result.PageResult;
import org.apache.ibatis.annotations.Select;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void StartOrStop(Integer status, long id);

    void update(EmployeeDTO employeeDTO);

    /**
     * 根据Id查询员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);
}
