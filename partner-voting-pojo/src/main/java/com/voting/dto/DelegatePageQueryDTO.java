package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 委托分页查询DTO
 */
@Data
@ApiModel(description = "委托分页查询DTO")
public class DelegatePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("委托人合伙人ID")
    private Long fromPartnerId;

    @ApiModelProperty("被委托人合伙人ID")
    private Long toPartnerId;

    @ApiModelProperty("状态（1有效 0无效）")
    private Integer status;

    @ApiModelProperty(value = "页码", required = true)
    private Integer page;

    @ApiModelProperty(value = "每页记录数", required = true)
    private Integer pageSize;
}
