package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 投票任务分页查询DTO
 */
@Data
@ApiModel(description = "投票任务分页查询DTO")
public class VoteTaskPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("议案ID")
    private Long proposalId;

    @ApiModelProperty("投票策略（1人数票 2出资票 3组合）")
    private Integer voteStrategy;

    @ApiModelProperty("状态（1投票中 2已结束）")
    private Integer status;

    @ApiModelProperty(value = "页码", required = true)
    private Integer page;

    @ApiModelProperty(value = "每页记录数", required = true)
    private Integer pageSize;
}
