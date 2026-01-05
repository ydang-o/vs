package com.voting.controller;

import com.github.pagehelper.Page;
import com.voting.dto.DelegateVoteSubmitDTO;
import com.voting.dto.VoteSubmitDTO;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.VoteService;
import com.voting.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H5移动端投票接口
 */
@RestController
@RequestMapping("/user/h5/vote")
@Api(tags = "H5移动端投票")
@Slf4j
public class H5VoteController {

    @Autowired
    private VoteService voteService;

    /**
     * 待投票任务列表
     */
    @GetMapping("/task/todo")
    @ApiOperation("待投票任务列表")
    public Result todoList(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("H5查询待投票列表，页码：{}，每页记录数：{}", pageNum, pageSize);
        Page<H5VoteTaskVO> pageResult = voteService.getH5TodoList(pageNum, pageSize);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getResult());
        
        return Result.success(data);
    }

    /**
     * 投票任务详情
     */
    @GetMapping("/task/detail/{voteTaskId}")
    @ApiOperation("投票任务详情")
    public Result taskDetail(@PathVariable Long voteTaskId) {
        log.info("H5查询投票任务详情，任务ID：{}", voteTaskId);
        H5VoteTaskDetailVO detail = voteService.getH5TaskDetail(voteTaskId);
        
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> proposal = new HashMap<>();
        proposal.put("title", detail.getTitle());
        proposal.put("content", detail.getContent());
        
        Map<String, Object> voteTask = new HashMap<>();
        voteTask.put("voteTaskId", detail.getVoteTaskId());
        voteTask.put("endTime", detail.getEndTime());
        voteTask.put("voteType", detail.getVoteType());
        voteTask.put("hasVoted", detail.getHasVoted());
        voteTask.put("canVote", detail.getCanVote());
        voteTask.put("canDelegateVote", detail.getCanDelegateVote());
        
        data.put("proposal", proposal);
        data.put("voteTask", voteTask);
        
        return Result.success(data);
    }

    /**
     * 本人投票
     */
    @PostMapping("/submit")
    @ApiOperation("本人投票")
    public Result submit(@RequestBody VoteSubmitDTO voteSubmitDTO) {
        log.info("H5提交投票：{}", voteSubmitDTO);
        voteService.submitVote(voteSubmitDTO.getVoteTaskId(), voteSubmitDTO.getVoteOption());
        return Result.success("投票成功");
    }

    /**
     * 委托投票详情查询
     */
    @GetMapping("/delegate/detail/{voteTaskId}")
    @ApiOperation("委托投票详情查询")
    public Result delegateDetail(@PathVariable Long voteTaskId) {
        log.info("H5查询委托投票详情，任务ID：{}", voteTaskId);
        DelegateDetailVO detail = voteService.getDelegateDetail(voteTaskId);
        return Result.success(detail);
    }

    /**
     * 提交委托投票
     */
    @PostMapping("/delegate/submit")
    @ApiOperation("提交委托投票")
    public Result delegateSubmit(@RequestBody DelegateVoteSubmitDTO dto) {
        log.info("H5提交委托投票：{}", dto);
        voteService.submitDelegateVote(dto.getVoteTaskId(), dto.getFromPartnerId(), dto.getVoteOption());
        return Result.success("委托投票成功");
    }

    /**
     * 已投票列表
     */
    @GetMapping("/task/done")
    @ApiOperation("已投票列表")
    public Result doneList(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("H5查询已投票列表，页码：{}，每页记录数：{}", pageNum, pageSize);
        Page<MyVoteTaskVO> pageResult = voteService.getH5DoneList(pageNum, pageSize);
        return Result.success(pageResult.getResult());
    }
}
