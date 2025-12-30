package com.voting.service;

import com.voting.dto.SysUserCreateDTO;
import com.voting.dto.SysUserPageQueryDTO;
import com.voting.dto.SysUserResetPasswordDTO;
import com.voting.dto.SysUserUpdateDTO;
import com.voting.result.PageResult;
import com.voting.dto.SysUserLoginDTO;
import com.voting.entity.SysUser;

public interface SysUserService {

    void add(SysUserCreateDTO sysUserCreateDTO);

    PageResult pageQuery(SysUserPageQueryDTO sysUserPageQueryDTO);

    void update(SysUserUpdateDTO sysUserUpdateDTO);

    void resetPassword(SysUserResetPasswordDTO resetPasswordDTO);

    void delete(Long id);

    SysUser login(SysUserLoginDTO loginDTO);
}
