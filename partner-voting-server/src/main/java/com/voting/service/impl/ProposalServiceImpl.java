package com.voting.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.voting.constant.MessageConstant;
import com.voting.context.BaseContext;
import com.voting.dto.ProposalAddDTO;
import com.voting.dto.ProposalPageQueryDTO;
import com.voting.dto.ProposalUpdateDTO;
import com.voting.entity.Proposal;
import com.voting.exception.BaseException;
import com.voting.mapper.ProposalMapper;
import com.voting.result.PageResult;
import com.voting.service.ProposalService;
import com.voting.vo.ProposalDetailVO;
import com.voting.vo.ProposalVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 议案服务实现类
 */
@Service
@Slf4j
public class ProposalServiceImpl implements ProposalService {

    @Autowired
    private ProposalMapper proposalMapper;

    /**
     * 新增议案（草稿）
     * @param proposalAddDTO 议案信息
     * @return 议案ID
     */
    @Override
    public Long add(ProposalAddDTO proposalAddDTO) {
        log.info("新增议案：{}", proposalAddDTO);

        // 生成议案编号
        String proposalNo = generateProposalNo();

        // 创建议案
        Proposal proposal = new Proposal();
        BeanUtils.copyProperties(proposalAddDTO, proposal);
        proposal.setProposalNo(proposalNo);
        proposal.setStatus(0); // 默认状态：草稿
        proposal.setCreatorId(BaseContext.getCurrentId()); // 拦截器已保证用户已登录
        proposal.setCreateTime(LocalDateTime.now());

        proposalMapper.insert(proposal);
        
        return proposal.getId();
    }

    /**
     * 议案列表分页查询
     * @param proposalPageQueryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(ProposalPageQueryDTO proposalPageQueryDTO) {
        log.info("议案列表查询：{}", proposalPageQueryDTO);

        PageHelper.startPage(proposalPageQueryDTO.getPageNum(), proposalPageQueryDTO.getPageSize());
        Page<ProposalVO> page = proposalMapper.pageQuery(proposalPageQueryDTO);

        long total = page.getTotal();
        List<ProposalVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 查看议案详情
     * @param id 议案ID
     * @return 议案详情
     */
    @Override
    public ProposalDetailVO getDetail(Long id) {
        log.info("查看议案详情：{}", id);

        ProposalDetailVO detailVO = proposalMapper.getDetailById(id);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.PROPOSAL_NOT_FOUND);
        }

        return detailVO;
    }

    /**
     * 编辑议案（仅草稿/待发布）
     * @param proposalUpdateDTO 议案信息
     */
    @Override
    public void update(ProposalUpdateDTO proposalUpdateDTO) {
        log.info("编辑议案信息：{}", proposalUpdateDTO);

        // 校验议案是否存在
        Proposal proposal = proposalMapper.getById(proposalUpdateDTO.getId());
        if (proposal == null) {
            throw new BaseException(MessageConstant.PROPOSAL_NOT_FOUND);
        }

        // 只有草稿和待发布状态可以修改
        if (proposal.getStatus() != 0 && proposal.getStatus() != 1) {
            throw new BaseException(MessageConstant.PROPOSAL_STATUS_NOT_ALLOW_UPDATE);
        }

        // 更新议案信息
        Proposal updateProposal = new Proposal();
        updateProposal.setId(proposalUpdateDTO.getId());
        updateProposal.setTitle(proposalUpdateDTO.getTitle());
        updateProposal.setContent(proposalUpdateDTO.getContent());

        proposalMapper.update(updateProposal);
    }

    /**
     * 删除议案（仅草稿）
     * @param id 议案ID
     */
    @Override
    public void delete(Long id) {
        log.info("删除议案：{}", id);

        // 校验议案是否存在
        Proposal proposal = proposalMapper.getById(id);
        if (proposal == null) {
            throw new BaseException(MessageConstant.PROPOSAL_NOT_FOUND);
        }

        // 只有草稿状态可以删除
        if (proposal.getStatus() != 0) {
            throw new BaseException(MessageConstant.PROPOSAL_STATUS_NOT_ALLOW_DELETE);
        }

        // 检查是否已关联投票任务
        int voteTaskCount = proposalMapper.countVoteTasksByProposalId(id);
        if (voteTaskCount > 0) {
            throw new BaseException(MessageConstant.PROPOSAL_HAS_VOTE_TASK);
        }

        // 删除议案
        proposalMapper.deleteById(id);
        log.info("议案删除成功，ID：{}", id);
    }

    /**
     * 发布议案
     * @param id 议案ID
     */
    @Override
    public void publish(Long id) {
        log.info("发布议案：{}", id);

        // 校验议案是否存在
        Proposal proposal = proposalMapper.getById(id);
        if (proposal == null) {
            throw new BaseException(MessageConstant.PROPOSAL_NOT_FOUND);
        }

        // 只有待发布状态可以发布
        if (proposal.getStatus() != 1) {
            throw new BaseException(MessageConstant.PROPOSAL_STATUS_NOT_ALLOW_PUBLISH);
        }

        // 更新状态为已发布，并记录发布时间
        proposalMapper.updateStatus(id, 2);
    }

    /**
     * 议案预览（发布前）
     * @param id 议案ID
     * @return 议案详情
     */
    @Override
    public ProposalDetailVO preview(Long id) {
        log.info("议案预览：{}", id);
        
        // 预览和详情返回一致，仅用于前端区分按钮状态
        return getDetail(id);
    }

    /**
     * 生成议案编号（格式：PA-2026-001）
     * @return 议案编号
     */
    private String generateProposalNo() {
        int year = LocalDateTime.now().getYear();
        int count = proposalMapper.countByYear(year);
        String serialNumber = String.format("%03d", count + 1);
        return "PA-" + year + "-" + serialNumber;
    }
}
