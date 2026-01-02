package com.voting.controller;

import com.github.pagehelper.Page;
import com.voting.constant.MessageConstant;
import com.voting.dto.VoteTaskCreateDTO;
import com.voting.dto.VoteTaskPageQueryDTO;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.VoteTaskService;
import com.voting.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vote/task")
@Api(tags = "投票任务管理")
@Slf4j
public class VoteTaskController {

    @Autowired
    private VoteTaskService voteTaskService;

    /**
     * 创建投票任务
     */
    @PostMapping("/create")
    @ApiOperation("创建投票任务")
    public Result create(@RequestBody VoteTaskCreateDTO voteTaskCreateDTO) {
        log.info("创建投票任务：{}", voteTaskCreateDTO);
        voteTaskService.createVoteTask(voteTaskCreateDTO);
        return Result.success();
    }

    /**
     * 投票任务列表
     */
    @GetMapping("/list")
    @ApiOperation("投票任务列表")
    public Result list(VoteTaskPageQueryDTO pageQueryDTO) {
        log.info("查询投票任务列表：{}", pageQueryDTO);
        PageResult<VoteTaskVO> pageResult = voteTaskService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 投票任务详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("投票任务详情")
    public Result detail(@PathVariable Long id) {
        log.info("查询投票任务详情：{}", id);
        VoteTaskDetailVO detail = voteTaskService.getDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建投票任务预览
     */
    @PostMapping("/preview")
    @ApiOperation("创建投票任务预览")
    public Result preview(@RequestBody VoteTaskCreateDTO voteTaskCreateDTO) {
        log.info("预览投票任务：{}", voteTaskCreateDTO);
        VoteTaskPreviewVO preview = voteTaskService.preview(voteTaskCreateDTO);
        return Result.success(preview);
    }

    /**
     * 投票进度统计
     */
    @GetMapping("/stat/{id}")
    @ApiOperation("投票进度统计")
    public Result stat(@PathVariable Long id) {
        log.info("查询投票进度统计：{}", id);
        VoteStatVO stat = voteTaskService.getStat(id);
        return Result.success(stat);
    }

    /**
     * 投票结果计算
     */
    @GetMapping("/result/{id}")
    @ApiOperation("投票结果计算")
    public Result result(@PathVariable Long id) {
        log.info("查询投票结果：{}", id);
        VoteResultVO result = voteTaskService.getResult(id);
        return Result.success(result);
    }

    /**
     * 提前结束投票
     */
    @PutMapping("/end/{id}")
    @ApiOperation("提前结束投票")
    public Result end(@PathVariable Long id) {
        log.info("结束投票任务：{}", id);
        voteTaskService.endVoteTask(id);
        return Result.success();
    }

    /**
     * 投票任务参与人列表
     */
    @GetMapping("/partners/{id}")
    @ApiOperation("投票任务参与人列表")
    public Result partners(@PathVariable Long id,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("查询投票任务参与人列表：{}，页码：{}，每页记录数：{}", id, page, pageSize);
        Page<VoteTaskPartnerVO> pageResult = voteTaskService.getPartners(id, page, pageSize);
        return Result.success(new PageResult<>(pageResult.getTotal(), pageResult.getResult()));
    }

    /**
     * 新增投票任务参与人
     */
    @PostMapping("/partners/{id}")
    @ApiOperation("新增投票任务参与人")
    public Result addPartner(@PathVariable Long id, @RequestParam Long partnerId) {
        log.info("新增投票任务参与人，taskId：{}，partnerId：{}", id, partnerId);
        voteTaskService.addPartner(id, partnerId);
        return Result.success();
    }
}
