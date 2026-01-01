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
 * 委托VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "委托VO")
public class DelegateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("委托ID")
    private Long id;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("议案标题")
    private String proposalTitle;

    @ApiModelProperty("委托人ID")
    private Long fromPartnerId;

    @ApiModelProperty("委托人姓名")
    private String fromPartnerName;

    @ApiModelProperty("被委托人ID")
    private Long toPartnerId;

    @ApiModelProperty("被委托人姓名")
    private String toPartnerName;

    @ApiModelProperty("委托证明文件路径")
    private String proofFile;

    @ApiModelProperty("状态（1有效 0无效）")
    private Integer status;

    @ApiModelProperty("委托时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
