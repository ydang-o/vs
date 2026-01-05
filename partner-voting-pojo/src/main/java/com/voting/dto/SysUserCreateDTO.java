package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "新增系统用户入参")
public class SysUserCreateDTO implements Serializable {

    @ApiModelProperty("登录用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("初始密码")
    private String password;
}
