package com.voting.service;

import com.voting.dto.UserLoginDTO;
import com.voting.entity.User;

public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
