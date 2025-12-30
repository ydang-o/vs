package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "系统用户分页查询入参")
public class SysUserPageQueryDTO implements Serializable {

    @ApiModelProperty("用户名模糊查询")
    private String username;

    @ApiModelProperty("手机号模糊查询")
    private String phone;

    @ApiModelProperty("状态：1正常 0禁用")
    private Integer status;

    @ApiModelProperty("页码")
    private int pageNum;

    @ApiModelProperty("页大小")
    private int pageSize;
}
