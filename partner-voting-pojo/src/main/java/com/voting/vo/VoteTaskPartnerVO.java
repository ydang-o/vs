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
 * 投票任务参与人VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票任务参与人VO")
public class VoteTaskPartnerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("合伙人ID")
    private Long partnerId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("合伙人姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("合伙人层级（1总部一级 2总部二级 3分部一级 4分部二级）")
    private Integer level;

    @ApiModelProperty("层级票权")
    private Integer levelWeight;

    @ApiModelProperty("出资比例（%）")
    private BigDecimal investRatio;

    @ApiModelProperty("是否已投票（0否 1是）")
    private Integer hasVoted;

    @ApiModelProperty("投票选项（1同意 2反对 3弃权）")
    private Integer voteOption;
}
