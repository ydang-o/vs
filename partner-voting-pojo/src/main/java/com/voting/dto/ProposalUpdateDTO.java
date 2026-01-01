package com.voting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑议案DTO
 */
@Data
@ApiModel(description = "编辑议案入参")
public class ProposalUpdateDTO implements Serializable {

    @ApiModelProperty(value = "议案ID", required = true)
    private Long id;

    @ApiModelProperty(value = "议案标题")
    private String title;

    @ApiModelProperty(value = "议案正文")
    private String content;
}
