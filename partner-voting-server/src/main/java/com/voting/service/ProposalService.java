package com.voting.service;

import com.voting.dto.ProposalAddDTO;
import com.voting.dto.ProposalPageQueryDTO;
import com.voting.dto.ProposalUpdateDTO;
import com.voting.result.PageResult;
import com.voting.vo.ProposalDetailVO;

/**
 * 议案服务接口
 */
public interface ProposalService {

    /**
     * 新增议案（草稿）
     * @param proposalAddDTO 议案信息
     * @return 议案ID
     */
    Long add(ProposalAddDTO proposalAddDTO);

    /**
     * 议案列表分页查询
     * @param proposalPageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(ProposalPageQueryDTO proposalPageQueryDTO);

    /**
     * 查看议案详情
     * @param id 议案ID
     * @return 议案详情
     */
    ProposalDetailVO getDetail(Long id);

    /**
     * 编辑议案（仅草稿/待发布）
     * @param proposalUpdateDTO 议案信息
     */
    void update(ProposalUpdateDTO proposalUpdateDTO);

    /**
     * 删除议案（仅草稿）
     * @param id 议案ID
     */
    void delete(Long id);

    /**
     * 发布议案
     * @param id 议案ID
     */
    void publish(Long id);

    /**
     * 议案预览（发布前）
     * @param id 议案ID
     * @return 议案详情
     */
    ProposalDetailVO preview(Long id);
}
