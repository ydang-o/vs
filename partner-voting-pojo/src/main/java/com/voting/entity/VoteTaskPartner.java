package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 投票任务参与人
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteTaskPartner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 投票任务ID
     */
    private Long voteTaskId;

    /**
     * 合伙人ID
     */
    private Long partnerId;

    /**
     * 层级票权（人数票用）
     */
    private Integer levelWeight;

    /**
     * 出资比例（出资票用）
     */
    private BigDecimal investRatio;

    /**
     * 是否已投票（0否 1是）
     */
    private Integer hasVoted;
}
