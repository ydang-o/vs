package com.voting.controller.user;

import com.voting.dto.PartnerPageQueryDTO;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.PartnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * C端合伙人接口
 */
@RestController("userPartnerController")
@RequestMapping("/user/partner")
@Api(tags = "C端合伙人相关接口")
@Slf4j
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    /**
     * 查询合伙人列表（仅启用状态，用于选择被委托人）
     */
    @GetMapping("/list")
    @ApiOperation("查询合伙人列表（仅启用状态）")
    public Result<Map<String, Object>> list(PartnerPageQueryDTO partnerPageQueryDTO) {
        log.info("C端查询合伙人列表: {}", partnerPageQueryDTO);
        
        // 强制设置status=1，只查询启用状态的合伙人
        partnerPageQueryDTO.setStatus(1);
        
        PageResult pageResult = partnerService.pageQuery(partnerPageQueryDTO);
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        
        return Result.success(data);
    }
}
