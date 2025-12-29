package com.voting.service;

import com.voting.dto.EmployeeDTO;
import com.voting.dto.EmployeeLoginDTO;
import com.voting.dto.EmployeePageQueryDTO;
import com.voting.entity.Employee;
import com.voting.result.PageResult;
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
