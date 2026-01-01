package com.voting.controller.admin;

import com.voting.dto.ProposalAddDTO;
import com.voting.dto.ProposalPageQueryDTO;
import com.voting.dto.ProposalUpdateDTO;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.ProposalService;
import com.voting.vo.ProposalDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 议案管理接口
 */
@RestController
@RequestMapping("/api/proposal")
@Api(tags = "议案管理接口")
@Slf4j
public class ProposalController {

    @Autowired
    private ProposalService proposalService;

    /**
     * 新增议案（草稿）
     */
    @PostMapping("/add")
    @ApiOperation("新增议案")
    public Result<Map<String, Object>> add(@RequestBody ProposalAddDTO proposalAddDTO) {
        log.info("新增议案: title={}", proposalAddDTO.getTitle());
        Long proposalId = proposalService.add(proposalAddDTO);
        
        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", proposalId);
        
        return Result.success(data);
    }

    /**
     * 议案列表查询（分页）
     */
    @GetMapping("/list")
    @ApiOperation("议案列表查询")
    public Result<Map<String, Object>> list(ProposalPageQueryDTO proposalPageQueryDTO) {
        log.info("议案列表查询: {}", proposalPageQueryDTO);
        PageResult pageResult = proposalService.pageQuery(proposalPageQueryDTO);
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        
        return Result.success(data);
    }

    /**
     * 查看议案详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("查看议案详情")
    public Result<ProposalDetailVO> getDetail(@PathVariable Long id) {
        log.info("查看议案详情: id={}", id);
        ProposalDetailVO detailVO = proposalService.getDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 编辑议案（仅草稿/待发布）
     */
    @PutMapping("/update")
    @ApiOperation("编辑议案")
    public Result<Void> update(@RequestBody ProposalUpdateDTO proposalUpdateDTO) {
        log.info("编辑议案: id={}", proposalUpdateDTO.getId());
        proposalService.update(proposalUpdateDTO);
        return Result.success();
    }

    /**
     * 删除议案（仅草稿）
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除议案")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除议案: id={}", id);
        proposalService.delete(id);
        return Result.success();
    }

    /**
     * 发布议案
     */
    @PutMapping("/publish/{id}")
    @ApiOperation("发布议案")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("发布议案: id={}", id);
        proposalService.publish(id);
        return Result.success();
    }

    /**
     * 议案预览（发布前）
     */
    @GetMapping("/preview/{id}")
    @ApiOperation("议案预览")
    public Result<ProposalDetailVO> preview(@PathVariable Long id) {
        log.info("议案预览: id={}", id);
        ProposalDetailVO detailVO = proposalService.preview(id);
        return Result.success(detailVO);
    }
}
