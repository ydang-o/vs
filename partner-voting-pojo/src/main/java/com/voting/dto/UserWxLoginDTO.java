package com.voting.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户微信登录DTO
 */
@Data
public class UserWxLoginDTO implements Serializable {

    // 微信授权码
    private String code;

    // 用户ID（预登录时获取）
    private Long userId;

}
