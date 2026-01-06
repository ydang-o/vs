package com.voting.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户预登录DTO（用户名密码验证）
 */
@Data
public class UserPreLoginDTO implements Serializable {

    // 用户名
    private String username;

    // 密码
    private String password;

}
