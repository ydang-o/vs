package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 议案分页查询DTO
 */
@Data
@ApiModel(description = "议案分页查询入参")
public class ProposalPageQueryDTO implements Serializable {

    @ApiModelProperty("标题模糊查询")
    private String title;

    @ApiModelProperty("状态：0草稿 1待发布 2已发布 3已关联投票 4已结束")
    private Integer status;

    @ApiModelProperty("页码")
    private int pageNum;

    @ApiModelProperty("页大小")
    private int pageSize;
}
