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
 * 投票任务预览VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票任务预览VO")
public class VoteTaskPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("符合条件的合伙人总数")
    private Integer totalPartners;

    @ApiModelProperty("总票权（人数票）")
    private Integer totalVotes;

    @ApiModelProperty("总出资比例（出资票）")
    private BigDecimal totalInvestRatio;

    @ApiModelProperty("通过所需票数（人数票）")
    private Integer requiredVotes;

    @ApiModelProperty("通过所需出资比例（出资票）")
    private BigDecimal requiredInvestRatio;
}
