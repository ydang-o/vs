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
}
