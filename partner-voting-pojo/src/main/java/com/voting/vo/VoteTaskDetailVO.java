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
 * 投票任务详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票任务详情VO")
public class VoteTaskDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long id;

    @ApiModelProperty("关联议案ID")
    private Long proposalId;

    @ApiModelProperty("议案标题")
    private String proposalTitle;

    @ApiModelProperty("议案编号")
    private String proposalNo;

    @ApiModelProperty("议案内容")
    private String proposalContent;

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

    @ApiModelProperty("通过阈值（%）")
    private Integer passRate;

    @ApiModelProperty("状态（1投票中 2已结束）")
    private Integer status;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("参与人数")
    private Integer partnerCount;

    @ApiModelProperty("已投票人数")
    private Integer votedCount;

    @ApiModelProperty("投票进度（%）")
    private Integer progress;

    @ApiModelProperty("同意票数")
    private Integer agreeCount;

    @ApiModelProperty("反对票数")
    private Integer rejectCount;

    @ApiModelProperty("弃权票数")
    private Integer abstainCount;
}
