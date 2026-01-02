package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 委托投票提交DTO
 */
@Data
@ApiModel(description = "委托投票提交DTO")
public class DelegateVoteSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("委托人合伙人ID")
    private Long fromPartnerId;

    @ApiModelProperty("投票选项（1同意 2反对 3弃权）")
    private Integer voteOption;
}
