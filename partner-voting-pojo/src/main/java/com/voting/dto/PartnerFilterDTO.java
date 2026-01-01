package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 合伙人筛选条件
 */
@Data
@ApiModel(description = "合伙人筛选条件")
public class PartnerFilterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("合伙人层级列表（1总部一级 2总部二级 3分部一级 4分部二级）")
    private List<Integer> levels;

    @ApiModelProperty("合伙人状态（1有效 0失效）")
    private Integer status;
}
