package com.voting.mapper;

import com.github.pagehelper.Page;
import com.voting.dto.SysUserPageQueryDTO;
import com.voting.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper {

    void insert(SysUser sysUser);

    Page<SysUser> pageQuery(SysUserPageQueryDTO sysUserPageQueryDTO);

    void update(SysUser sysUser);

    SysUser getById(Long id);

    void updatePassword(@Param("id") Long id, @Param("password") String password);

    void deleteById(Long id);

    SysUser getByUsername(String username);

    void updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
