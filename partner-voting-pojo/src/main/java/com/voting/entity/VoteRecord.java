package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投票记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票记录ID
     */
    private Long id;

    /**
     * 投票任务ID
     */
    private Long voteTaskId;

    /**
     * 投票人合伙人ID
     */
    private Long partnerId;

    /**
     * 投票选项（1同意 2反对 3弃权）
     */
    private Integer voteOption;

    /**
     * 投票时间
     */
    private LocalDateTime voteTime;

    /**
     * 是否委托投票（0否 1是）
     */
    private Integer isDelegate;

    /**
     * 被委托人ID
     */
    private Long delegatePartnerId;
}
