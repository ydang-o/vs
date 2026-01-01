package com.voting.service;

import com.github.pagehelper.Page;
import com.voting.dto.VoteTaskCreateDTO;
import com.voting.dto.VoteTaskPageQueryDTO;
import com.voting.result.PageResult;
import com.voting.vo.*;

public interface VoteTaskService {

    /**
     * 创建投票任务
     */
    void createVoteTask(VoteTaskCreateDTO voteTaskCreateDTO);

    /**
     * 投票任务列表分页查询
     */
    PageResult<VoteTaskVO> pageQuery(VoteTaskPageQueryDTO pageQueryDTO);

    /**
     * 查询投票任务详情
     */
    VoteTaskDetailVO getDetail(Long id);

    /**
     * 创建投票任务预览
     */
    VoteTaskPreviewVO preview(VoteTaskCreateDTO voteTaskCreateDTO);

    /**
     * 查询投票进度统计
     */
    VoteStatVO getStat(Long id);

    /**
     * 查询投票结果
     */
    VoteResultVO getResult(Long id);

    /**
     * 提前结束投票
     */
    void endVoteTask(Long id);

    /**
     * 查询投票任务参与人列表
     */
    Page<VoteTaskPartnerVO> getPartners(Long id, Integer page, Integer pageSize);
}
