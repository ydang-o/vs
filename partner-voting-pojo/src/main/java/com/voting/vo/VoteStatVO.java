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
 * 投票统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票统计VO")
public class VoteStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @ApiModelProperty("同意占比（%）")
    private BigDecimal agreeRatio;

    @ApiModelProperty("反对占比（%）")
    private BigDecimal rejectRatio;

    @ApiModelProperty("弃权占比（%）")
    private BigDecimal abstainRatio;
}
