package com.voting.service;

import com.voting.dto.SysUserCreateDTO;
import com.voting.dto.SysUserPageQueryDTO;
import com.voting.dto.SysUserResetPasswordDTO;
import com.voting.dto.SysUserUpdateDTO;
import com.voting.dto.PasswordEditDTO;
import com.voting.result.PageResult;
import com.voting.dto.SysUserLoginDTO;
import com.voting.entity.SysUser;

public interface SysUserService {

    void add(SysUserCreateDTO sysUserCreateDTO);

    PageResult pageQuery(SysUserPageQueryDTO sysUserPageQueryDTO);

    void update(SysUserUpdateDTO sysUserUpdateDTO);

    void resetPassword(SysUserResetPasswordDTO resetPasswordDTO);

    /**
     * 用户自助修改密码（校验旧密码）
     */
    void changePassword(PasswordEditDTO passwordEditDTO);

    void delete(Long id);

    SysUser login(SysUserLoginDTO loginDTO);
}
