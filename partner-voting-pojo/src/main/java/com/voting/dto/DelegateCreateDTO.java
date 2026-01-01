package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建委托DTO
 */
@Data
@ApiModel(description = "创建委托DTO")
public class DelegateCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "投票任务ID", required = true)
    private Long voteTaskId;

    @ApiModelProperty(value = "委托人合伙人ID", required = true)
    private Long fromPartnerId;

    @ApiModelProperty(value = "被委托人合伙人ID", required = true)
    private Long toPartnerId;

    @ApiModelProperty(value = "委托证明文件路径", required = true)
    private String proofFile;
}
