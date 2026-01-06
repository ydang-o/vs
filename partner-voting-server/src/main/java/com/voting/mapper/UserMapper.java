package com.voting.mapper;

import com.voting.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from sys_user where openid=#{openid}")
    SysUser getByOpenId(String openid);

    /**
     * 插入数据
     * @param sysUser
     */
    void insert(SysUser sysUser);

    /**
     * 根据ID查询用户
     * @param userId
     * @return
     */
    @Select("select * from sys_user where id=#{id}")
    SysUser getById(Long userId);

    /**
     * 根据条件动态统计
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 根据姓名和手机号查询用户
     * @param name
     * @param phone
     * @return
     */
    @Select("select * from sys_user where name=#{name} and phone=#{phone}")
    SysUser getByNameAndPhone(String name, String phone);

    /**
     * 更新用户信息
     * @param sysUser
     */
    void update(SysUser sysUser);

    /**
     * 根据ID删除用户
     * @param id
     */
    @Select("delete from sys_user where id=#{id}")
    void deleteById(Long id);
}
