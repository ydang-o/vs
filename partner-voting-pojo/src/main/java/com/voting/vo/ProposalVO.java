package com.voting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 议案列表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "议案列表信息")
public class ProposalVO implements Serializable {

    @ApiModelProperty("议案ID")
    private Long id;

    @ApiModelProperty("议案编号")
    private String proposalNo;

    @ApiModelProperty("议案标题")
    private String title;

    @ApiModelProperty("状态：0草稿 1待发布 2已发布 3已关联投票 4已结束")
    private Integer status;

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
