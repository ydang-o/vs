package com.voting.controller.admin;

import com.voting.dto.PartnerAddDTO;
import com.voting.dto.PartnerPageQueryDTO;
import com.voting.dto.PartnerUpdateDTO;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.PartnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 合伙人管理接口
 */
@RestController
@RequestMapping("/admin/partner")
@Api(tags = "合伙人管理接口")
@Slf4j
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    /**
     * 新增合伙人（用户关联）
     */
    @PostMapping("/add")
    @ApiOperation("新增合伙人")
    public Result<Void> add(@RequestBody PartnerAddDTO partnerAddDTO) {
        log.info("新增合伙人: userId={}, level={}, investRatio={}", 
                partnerAddDTO.getUserId(), partnerAddDTO.getLevel(), partnerAddDTO.getInvestRatio());
        partnerService.add(partnerAddDTO);
        return Result.success();
    }

    /**
     * 合伙人列表查询
     */
    @GetMapping("/list")
    @ApiOperation("合伙人列表查询")
    public Result<Map<String, Object>> list(PartnerPageQueryDTO partnerPageQueryDTO) {
        log.info("合伙人列表查询: {}", partnerPageQueryDTO);
        PageResult pageResult = partnerService.pageQuery(partnerPageQueryDTO);
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        
        return Result.success(data);
    }

    /**
     * 编辑合伙人信息
     */
    @PutMapping("/update")
    @ApiOperation("编辑合伙人信息")
    public Result<Void> update(@RequestBody PartnerUpdateDTO partnerUpdateDTO) {
        log.info("编辑合伙人信息: partnerId={}, level={}, investRatio={}", 
                partnerUpdateDTO.getPartnerId(), partnerUpdateDTO.getLevel(), partnerUpdateDTO.getInvestRatio());
        partnerService.update(partnerUpdateDTO);
        return Result.success();
    }

    /**
     * 禁用合伙人
     */
    @PutMapping("/disable/{partnerId}")
    @ApiOperation("禁用合伙人")
    public Result<Void> disable(@PathVariable Long partnerId) {
        log.info("禁用合伙人: partnerId={}", partnerId);
        partnerService.disable(partnerId);
        return Result.success();
    }

    /**
     * 删除合伙人（仅未参与任何投票）
     */
    @DeleteMapping("/delete/{partnerId}")
    @ApiOperation("删除合伙人")
    public Result<Void> delete(@PathVariable Long partnerId) {
        log.info("删除合伙人: partnerId={}", partnerId);
        partnerService.delete(partnerId);
        return Result.success();
    }
}
