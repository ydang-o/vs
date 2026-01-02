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
 * H5待投票任务VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "H5待投票任务VO")
public class H5VoteTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("议案标题")
    private String proposalTitle;

    @ApiModelProperty("截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("是否紧急（距离截止<24小时）")
    private Boolean urgent;
}
