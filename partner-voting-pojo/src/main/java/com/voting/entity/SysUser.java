package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 登录用户名（管理员使用，微信用户可为空）
    private String username;

    // 姓名
    private String name;

    // 手机号
    private String phone;

    // 微信用户唯一标识（微信用户使用，管理员可为空）
    private String openid;

    // 用户类型：1=管理员 2=微信用户
    private Integer userType;

    // 头像
    private String avatar;

    // 性别：0女 1男
    private Integer sex;

    // 身份证号
    private String idNumber;

    // 登录密码（管理员使用，微信用户可为空）
    private String password;

    // 状态：1正常 0禁用
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 最后登录时间
    private LocalDateTime lastLoginTime;
}
