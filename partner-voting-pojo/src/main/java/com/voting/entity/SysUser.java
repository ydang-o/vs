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

    private String username;

    private String realName;

    private String phone;

    private String password;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime lastLoginTime;
}
