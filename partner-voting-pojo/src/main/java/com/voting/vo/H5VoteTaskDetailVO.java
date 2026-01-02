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
 * H5投票任务详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "H5投票任务详情VO")
public class H5VoteTaskDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("议案标题")
    private String title;

    @ApiModelProperty("议案内容")
    private String content;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("投票方式（1匿名 2实名）")
    private Integer voteType;

    @ApiModelProperty("本人是否已投票")
    private Boolean hasVoted;

    @ApiModelProperty("是否可本人投票")
    private Boolean canVote;

    @ApiModelProperty("是否可代他人投票")
    private Boolean canDelegateVote;
}
