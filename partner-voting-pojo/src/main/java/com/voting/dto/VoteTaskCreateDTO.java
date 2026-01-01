package com.voting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建投票任务DTO
 */
@Data
@ApiModel(description = "创建投票任务DTO")
public class VoteTaskCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联议案ID", required = true)
    private Long proposalId;

    @ApiModelProperty(value = "投票开始时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "投票截止时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "投票方式（1匿名 2实名）", required = true)
    private Integer voteType;

    @ApiModelProperty(value = "投票策略（1人数票 2出资票 3组合）", required = true)
    private Integer voteStrategy;

    @ApiModelProperty(value = "通过阈值（%）", required = true)
    private Integer passRate;

    @ApiModelProperty(value = "合伙人筛选条件", required = true)
    private PartnerFilterDTO partnerFilter;
}
