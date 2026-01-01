package com.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合伙人实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Partner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 合伙人ID
     */
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 合伙人层级：1总部一级 2总部二级 3分部一级 4分部二级
     */
    private Integer level;

    /**
     * 出资比例（%）
     */
    private BigDecimal investRatio;

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;

    /**
     * 关联时间
     */
    private LocalDateTime bindTime;
}
