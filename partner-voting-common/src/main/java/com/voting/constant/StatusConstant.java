package com.voting.constant;

/**
 * 状态常量，启用或者禁用
 */
public class StatusConstant {

    //启用
    public static final Integer ENABLE = 1;

    //禁用
    public static final Integer DISABLE = 0;
    
    /**
     * 议案状态
     */
    public static final Integer PROPOSAL_DRAFT = 0;      // 草稿
    public static final Integer PROPOSAL_PENDING = 1;    // 待发布
    public static final Integer PROPOSAL_PUBLISHED = 2;  // 已发布
    public static final Integer PROPOSAL_LINKED = 3;     // 已关联投票
    public static final Integer PROPOSAL_ENDED = 4;      // 已结束
    
    /**
     * 投票任务状态
     */
    public static final Integer VOTE_TASK_IN_PROGRESS = 1;  // 投票中
    public static final Integer VOTE_TASK_ENDED = 2;        // 已结束
    
    /**
     * 投票方式
     */
    public static final Integer VOTE_TYPE_ANONYMOUS = 1;  // 匿名
    public static final Integer VOTE_TYPE_REAL_NAME = 2;  // 实名
    
    /**
     * 投票策略
     */
    public static final Integer VOTE_STRATEGY_PEOPLE = 1;    // 人数票
    public static final Integer VOTE_STRATEGY_CAPITAL = 2;   // 出资票
    public static final Integer VOTE_STRATEGY_COMBINED = 3;  // 组合
    
    /**
     * 投票选项
     */
    public static final Integer VOTE_OPTION_AGREE = 1;    // 同意
    public static final Integer VOTE_OPTION_REJECT = 2;   // 反对
    public static final Integer VOTE_OPTION_ABSTAIN = 3;  // 弃权
    
    /**
     * 合伙人层级票权映射
     */
    public static final Integer LEVEL_WEIGHT_1 = 4;  // 总部一级：4票
    public static final Integer LEVEL_WEIGHT_2 = 3;  // 总部二级：3票
    public static final Integer LEVEL_WEIGHT_3 = 2;  // 分部一级：2票
    public static final Integer LEVEL_WEIGHT_4 = 1;  // 分部二级：1票
}

