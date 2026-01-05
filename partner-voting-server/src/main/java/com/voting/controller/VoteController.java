package com.voting.controller;

import com.github.pagehelper.Page;
import com.voting.dto.DelegateCreateDTO;
import com.voting.dto.DelegatePageQueryDTO;
import com.voting.dto.VoteSubmitDTO;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.VoteService;
import com.voting.vo.DelegateVO;
import com.voting.vo.MyVoteTaskVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/vote")
@Api(tags = "投票管理")
@Slf4j
public class VoteController {

    @Autowired
    private VoteService voteService;

    /**
     * 我的待投票列表
     */
    @GetMapping("/my/pending")
    @ApiOperation("我的待投票列表")
    public Result myPending(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("查询我的待投票列表，页码：{}，每页记录数：{}", page, pageSize);
        Page<MyVoteTaskVO> pageResult = voteService.getMyPendingVotes(page, pageSize);
        return Result.success(new PageResult<>(pageResult.getTotal(), pageResult.getResult()));
    }

    /**
     * 我的已投票列表
     */
    @GetMapping("/my/voted")
    @ApiOperation("我的已投票列表")
    public Result myVoted(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("查询我的已投票列表，页码：{}，每页记录数：{}", page, pageSize);
        Page<MyVoteTaskVO> pageResult = voteService.getMyVotedList(page, pageSize);
        return Result.success(new PageResult<>(pageResult.getTotal(), pageResult.getResult()));
    }

    /**
     * 提交投票
     */
    @PostMapping("/submit")
    @ApiOperation("提交投票")
    public Result submit(@RequestBody VoteSubmitDTO voteSubmitDTO) {
        log.info("提交投票：{}", voteSubmitDTO);
        voteService.submitVote(voteSubmitDTO.getVoteTaskId(), voteSubmitDTO.getVoteOption());
        return Result.success();
    }

    /**
     * 创建委托
     */
    @PostMapping("/task/delegate/create")
    @ApiOperation("创建委托")
    public Result createDelegate(@RequestBody DelegateCreateDTO delegateCreateDTO) {
        log.info("创建委托：{}", delegateCreateDTO);
        voteService.createDelegate(delegateCreateDTO);
        return Result.success();
    }

    /**
     * 委托管理列表
     */
    @GetMapping("/task/delegate/list")
    @ApiOperation("委托管理列表")
    public Result delegateList(DelegatePageQueryDTO pageQueryDTO) {
        log.info("查询委托列表：{}", pageQueryDTO);
        PageResult<DelegateVO> pageResult = voteService.getDelegateList(pageQueryDTO);
        return Result.success(pageResult);
    }
}
