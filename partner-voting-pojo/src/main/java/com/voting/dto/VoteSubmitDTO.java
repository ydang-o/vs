package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交投票DTO
 */
@Data
@ApiModel(description = "提交投票DTO")
public class VoteSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "投票任务ID", required = true)
    private Long voteTaskId;

    @ApiModelProperty(value = "投票选项（1同意 2反对 3弃权）", required = true)
    private Integer voteOption;
}
