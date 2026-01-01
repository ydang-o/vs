package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 议案实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proposal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 议案ID
     */
    private Long id;

    /**
     * 议案标题
     */
    private String title;

    /**
     * 议案编号
     */
    private String proposalNo;

    /**
     * 议案正文
     */
    private String content;

    /**
     * 状态：0草稿 1待发布 2已发布 3已关联投票 4已结束
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
}
