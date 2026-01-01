package com.voting.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 投票结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票结果VO")
public class VoteResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("投票策略（1人数票 2出资票 3组合）")
    private Integer voteStrategy;

    @ApiModelProperty("通过阈值（%）")
    private Integer passRate;

    @ApiModelProperty("人数票-同意票数")
    private Integer peopleAgreeVotes;

    @ApiModelProperty("人数票-反对票数")
    private Integer peopleRejectVotes;

    @ApiModelProperty("人数票-弃权票数")
    private Integer peopleAbstainVotes;

    @ApiModelProperty("人数票-有效总票数")
    private Integer peopleTotalVotes;

    @ApiModelProperty("人数票-同意得票率（%）")
    private BigDecimal peopleAgreeRatio;

    @ApiModelProperty("人数票-是否通过")
    private Boolean peoplePassed;

    @ApiModelProperty("出资票-同意出资比例")
    private BigDecimal capitalAgreeRatio;

    @ApiModelProperty("出资票-反对出资比例")
    private BigDecimal capitalRejectRatio;

    @ApiModelProperty("出资票-弃权出资比例")
    private BigDecimal capitalAbstainRatio;

    @ApiModelProperty("出资票-有效总出资比例")
    private BigDecimal capitalTotalRatio;

    @ApiModelProperty("出资票-同意得票率（%）")
    private BigDecimal capitalAgreeVoteRatio;

    @ApiModelProperty("出资票-是否通过")
    private Boolean capitalPassed;

    @ApiModelProperty("最终投票结果（true通过 false未通过）")
    private Boolean finalResult;

    @ApiModelProperty("结果说明")
    private String resultDesc;
}
