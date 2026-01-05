package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "编辑系统用户入参")
public class SysUserUpdateDTO implements Serializable {

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("真实姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("状态：1正常 0禁用")
    private Integer status;
}
