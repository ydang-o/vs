package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 编辑合伙人DTO
 */
@Data
@ApiModel(description = "编辑合伙人入参")
public class PartnerUpdateDTO implements Serializable {

    @ApiModelProperty(value = "合伙人ID", required = true)
    private Long partnerId;

    @ApiModelProperty(value = "合伙人层级：1总部一级 2总部二级 3分部一级 4分部二级")
    private Integer level;

    @ApiModelProperty(value = "出资比例（%）")
    private BigDecimal investRatio;
}
