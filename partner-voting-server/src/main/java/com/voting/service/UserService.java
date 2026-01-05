package com.voting.service;

import com.voting.dto.UserLoginDTO;
import com.voting.entity.SysUser;

public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    SysUser wxLogin(UserLoginDTO userLoginDTO);
}
