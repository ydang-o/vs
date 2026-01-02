package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投票任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票任务ID
     */
    private Long id;

    /**
     * 关联议案ID
     */
    private Long proposalId;

    /**
     * 投票开始时间
     */
    private LocalDateTime startTime;

    /**
     * 投票截止时间
     */
    private LocalDateTime endTime;

    /**
     * 投票方式（1匿名 2实名）
     */
    private Integer voteType;

    /**
     * 投票策略（1人数票 2出资票 3组合）
     */
    private Integer voteStrategy;

    /**
     * 通过阈值（%）
     */
    private Integer passRate;

    /**
     * 状态（1投票中 2已结束）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
