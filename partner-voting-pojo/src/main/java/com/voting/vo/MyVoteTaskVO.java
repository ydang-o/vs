package com.voting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的投票任务VO（移动端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "我的投票任务VO")
public class MyVoteTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long id;

    @ApiModelProperty("议案标题")
    private String proposalTitle;

    @ApiModelProperty("议案编号")
    private String proposalNo;

    @ApiModelProperty("投票开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty("投票截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("投票方式（1匿名 2实名）")
    private Integer voteType;

    @ApiModelProperty("投票策略（1人数票 2出资票 3组合）")
    private Integer voteStrategy;

    @ApiModelProperty("状态（1投票中 2已结束）")
    private Integer status;

    @ApiModelProperty("是否已投票（0否 1是）")
    private Integer hasVoted;

    @ApiModelProperty("我的投票选项（1同意 2反对 3弃权）")
    private Integer myVoteOption;

    @ApiModelProperty("投票时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voteTime;

    @ApiModelProperty("是否被委托（0否 1是）")
    private Integer isDelegated;
}
