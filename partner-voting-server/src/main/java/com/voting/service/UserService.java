package com.voting.service;

import com.voting.dto.UserBindDTO;
import com.voting.dto.UserLoginDTO;
import com.voting.dto.UserPreLoginDTO;
import com.voting.dto.UserWxLoginDTO;
import com.voting.entity.SysUser;

public interface UserService {
    /**
     * 微信登录（旧版，已废弃）
     * @param userLoginDTO
     * @return
     */
    @Deprecated
    SysUser wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 绑定用户
     * @param userBindDTO
     * @param currentUserId 当前微信登录用户ID
     * @return
     */
    SysUser bindUser(UserBindDTO userBindDTO, Long currentUserId);

    /**
     * 用户预登录（用户名密码验证）
     * @param userPreLoginDTO
     * @return
     */
    SysUser preLogin(UserPreLoginDTO userPreLoginDTO);

    /**
     * 微信登录（新版）
     * @param userWxLoginDTO
     * @return
     */
    SysUser wxLoginNew(UserWxLoginDTO userWxLoginDTO);
}
