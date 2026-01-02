package com.voting.service;

import com.github.pagehelper.Page;
import com.voting.dto.DelegateCreateDTO;
import com.voting.dto.DelegatePageQueryDTO;
import com.voting.result.PageResult;
import com.voting.vo.*;

import java.util.List;

public interface VoteService {

    /**
     * 我的待投票列表
     */
    Page<MyVoteTaskVO> getMyPendingVotes(Integer page, Integer pageSize);

    /**
     * 我的已投票列表
     */
    Page<MyVoteTaskVO> getMyVotedList(Integer page, Integer pageSize);

    /**
     * 提交投票
     */
    void submitVote(Long voteTaskId, Integer voteOption);

    /**
     * 创建委托
     */
    void createDelegate(DelegateCreateDTO delegateCreateDTO);

    /**
     * 委托管理列表
     */
    PageResult<DelegateVO> getDelegateList(DelegatePageQueryDTO pageQueryDTO);

    // ==================== H5接口 ====================

    /**
     * H5待投票任务列表
     */
    Page<H5VoteTaskVO> getH5TodoList(Integer page, Integer pageSize);

    /**
     * H5投票任务详情
     */
    H5VoteTaskDetailVO getH5TaskDetail(Long voteTaskId);

    /**
     * H5委托投票详情查询
     */
    DelegateDetailVO getDelegateDetail(Long voteTaskId);

    /**
     * H5提交委托投票
     */
    void submitDelegateVote(Long voteTaskId, Long fromPartnerId, Integer voteOption);

    /**
     * H5已投票列表
     */
    Page<MyVoteTaskVO> getH5DoneList(Integer page, Integer pageSize);

    // ==================== 结果统计接口 ====================

    /**
     * 投票结果汇总
     */
    VoteSummaryVO getVoteSummary(Long voteTaskId);

    /**
     * 投票明细查询（实名模式专用）
     */
    List<VoteDetailRecordVO> getVoteDetail(Long voteTaskId);

    /**
     * 导出投票结果Excel
     */
    void exportVoteResult(Long voteTaskId, javax.servlet.http.HttpServletResponse response);
}
