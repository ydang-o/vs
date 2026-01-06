package com.voting.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class SysUserVO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime lastLoginTime;
}
