package com.voting.controller;

import com.voting.result.Result;
import com.voting.service.VoteService;
import com.voting.vo.VoteDetailRecordVO;
import com.voting.vo.VoteSummaryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 投票结果统计接口
 */
@RestController
@RequestMapping("/api/vote/result")
@Api(tags = "投票结果统计")
@Slf4j
public class VoteResultController {

    @Autowired
    private VoteService voteService;

    /**
     * 投票结果汇总查询
     */
    @GetMapping("/summary/{voteTaskId}")
    @ApiOperation("投票结果汇总查询")
    public Result summary(@PathVariable Long voteTaskId) {
        log.info("查询投票结果汇总，任务ID：{}", voteTaskId);
        VoteSummaryVO summary = voteService.getVoteSummary(voteTaskId);
        return Result.success(summary);
    }

    /**
     * 投票明细查询（实名模式专用）
     */
    @GetMapping("/detail/{voteTaskId}")
    @ApiOperation("投票明细查询")
    public Result detail(@PathVariable Long voteTaskId) {
        log.info("查询投票明细，任务ID：{}", voteTaskId);
        List<VoteDetailRecordVO> detail = voteService.getVoteDetail(voteTaskId);
        return Result.success(detail);
    }

    /**
     * 导出投票结果
     */
    @GetMapping("/export/{voteTaskId}")
    @ApiOperation("导出投票结果")
    public void export(@PathVariable Long voteTaskId, javax.servlet.http.HttpServletResponse response) {
        log.info("导出投票结果，任务ID：{}", voteTaskId);
        voteService.exportVoteResult(voteTaskId, response);
    }
}
