package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增议案DTO
 */
@Data
@ApiModel(description = "新增议案入参")
public class ProposalAddDTO implements Serializable {

    @ApiModelProperty(value = "议案标题", required = true)
    private String title;

    @ApiModelProperty(value = "议案正文", required = true)
    private String content;
}
