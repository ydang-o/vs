package com.voting.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SysUserLoginVO implements Serializable {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String token;
}
