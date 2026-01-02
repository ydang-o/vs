package com.voting.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 委托投票详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "委托投票详情VO")
public class DelegateDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("委托人ID")
    private Long fromPartnerId;

    @ApiModelProperty("委托人姓名")
    private String fromPartnerName;

    @ApiModelProperty("委托人是否已投票")
    private Boolean hasVoted;
}
