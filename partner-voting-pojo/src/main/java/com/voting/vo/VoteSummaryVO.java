package com.voting.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 投票结果汇总VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "投票结果汇总VO")
public class VoteSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投票任务ID")
    private Long voteTaskId;

    @ApiModelProperty("议案标题")
    private String proposalTitle;

    @ApiModelProperty("投票策略（1人数票 2出资票 3组合）")
    private Integer voteStrategy;

    @ApiModelProperty("通过阈值")
    private Integer passRate;

    @ApiModelProperty("人数票结果")
    private PeopleVoteResult peopleVote;

    @ApiModelProperty("出资票结果")
    private CapitalVoteResult capitalVote;

    @ApiModelProperty("最终结果")
    private String finalResult;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeopleVoteResult implements Serializable {
        @ApiModelProperty("同意票数")
        private Integer agree;

        @ApiModelProperty("反对票数")
        private Integer reject;

        @ApiModelProperty("弃权票数")
        private Integer abstain;

        @ApiModelProperty("同意率")
        private String agreeRate;

        @ApiModelProperty("是否通过")
        private Boolean pass;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapitalVoteResult implements Serializable {
        @ApiModelProperty("同意率")
        private String agreeRate;

        @ApiModelProperty("是否通过")
        private Boolean pass;
    }
}
