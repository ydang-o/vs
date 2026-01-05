package com.voting.mapper;

import com.github.pagehelper.Page;
import com.voting.dto.EmployeePageQueryDTO;
import com.voting.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select id, username, name, password, phone, status, create_time from sys_user where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工进MySQL
     * @param employee
     */
        @Insert("insert into sys_user (username, name, phone, password, status, create_time)"
          +" values (#{username}, #{name}, #{phone}, #{password}, #{status}, #{createTime})")
    void insert(Employee employee);


    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
    void update(Employee employee);
        @Select("select id, username, name, password, phone, status, create_time from sys_user where id=#{id}")
    Employee getById(Long id);
}
