package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 委托投票
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 委托ID
     */
    private Long id;

    /**
     * 投票任务ID
     */
    private Long voteTaskId;

    /**
     * 委托人ID
     */
    private Long fromPartnerId;

    /**
     * 被委托人ID
     */
    private Long toPartnerId;

    /**
     * 委托证明文件路径
     */
    private String proofFile;

    /**
     * 状态（1有效 0无效）
     */
    private Integer status;

    /**
     * 委托时间
     */
    private LocalDateTime createTime;
}
