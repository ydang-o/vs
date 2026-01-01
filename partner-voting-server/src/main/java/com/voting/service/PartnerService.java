package com.voting.service;

import com.voting.dto.PartnerAddDTO;
import com.voting.dto.PartnerPageQueryDTO;
import com.voting.dto.PartnerUpdateDTO;
import com.voting.result.PageResult;

/**
 * 合伙人服务接口
 */
public interface PartnerService {

    /**
     * 新增合伙人（用户关联）
     * @param partnerAddDTO 合伙人信息
     */
    void add(PartnerAddDTO partnerAddDTO);

    /**
     * 合伙人列表分页查询
     * @param partnerPageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(PartnerPageQueryDTO partnerPageQueryDTO);

    /**
     * 编辑合伙人信息
     * @param partnerUpdateDTO 合伙人信息
     */
    void update(PartnerUpdateDTO partnerUpdateDTO);

    /**
     * 禁用合伙人
     * @param partnerId 合伙人ID
     */
    void disable(Long partnerId);

    /**
     * 删除合伙人（仅未参与任何投票）
     * @param partnerId 合伙人ID
     */
    void delete(Long partnerId);
}
