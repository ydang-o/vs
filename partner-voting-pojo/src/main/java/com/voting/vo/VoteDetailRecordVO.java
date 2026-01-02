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
 * 投票明细记录VO（实名模式专用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票明细记录VO")
public class VoteDetailRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("合伙人姓名")
    private String partnerName;

    @ApiModelProperty("投票选项")
    private String voteOption;

    @ApiModelProperty("投票时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voteTime;

    @ApiModelProperty("是否委托投票")
    private Boolean isDelegate;

    @ApiModelProperty("委托人姓名")
    private String delegateBy;
}
