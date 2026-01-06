package com.voting.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户绑定DTO
 */
@Data
public class UserBindDTO implements Serializable {

    // 姓名
    private String name;

    // 手机号
    private String phone;

}
