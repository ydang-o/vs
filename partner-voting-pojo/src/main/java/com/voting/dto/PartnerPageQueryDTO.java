package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 合伙人分页查询DTO
 */
@Data
@ApiModel(description = "合伙人分页查询入参")
public class PartnerPageQueryDTO implements Serializable {

    @ApiModelProperty("合伙人层级列表（可多选）：1总部一级 2总部二级 3分部一级 4分部二级")
    private List<Integer> levels;

    @ApiModelProperty("状态：1正常 0禁用")
    private Integer status;

    @ApiModelProperty("页码")
    private int pageNum;

    @ApiModelProperty("页大小")
    private int pageSize;
}
