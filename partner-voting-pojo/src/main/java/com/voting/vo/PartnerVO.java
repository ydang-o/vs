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
 * 合伙人VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "合伙人信息")
public class PartnerVO implements Serializable {

    @ApiModelProperty("合伙人ID")
    private Long partnerId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("合伙人层级：1总部一级 2总部二级 3分部一级 4分部二级")
    private Integer level;

    @ApiModelProperty("出资比例（%）")
    private BigDecimal investRatio;

    @ApiModelProperty("状态：1正常 0禁用")
    private Integer status;
}
