package com.voting.service;

import com.github.pagehelper.Page;
import com.voting.dto.DelegateCreateDTO;
import com.voting.dto.DelegatePageQueryDTO;
import com.voting.result.PageResult;
import com.voting.vo.DelegateVO;
import com.voting.vo.MyVoteTaskVO;

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
}
