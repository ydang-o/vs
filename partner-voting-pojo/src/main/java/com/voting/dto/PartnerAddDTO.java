package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 新增合伙人DTO
 */
@Data
@ApiModel(description = "新增合伙人入参")
public class PartnerAddDTO implements Serializable {

    @ApiModelProperty(value = "关联用户ID", required = true)
    private Long userId;

    @ApiModelProperty(value = "合伙人层级：1总部一级 2总部二级 3分部一级 4分部二级", required = true)
    private Integer level;

    @ApiModelProperty(value = "出资比例（%）", required = true)
    private BigDecimal investRatio;
}
