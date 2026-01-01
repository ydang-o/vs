package com.voting.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.voting.constant.MessageConstant;
import com.voting.constant.StatusConstant;
import com.voting.context.BaseContext;
import com.voting.dto.PartnerFilterDTO;
import com.voting.exception.BaseException;
import com.voting.dto.VoteTaskCreateDTO;
import com.voting.dto.VoteTaskPageQueryDTO;
import com.voting.entity.*;
import com.voting.mapper.*;
import com.voting.result.PageResult;
import com.voting.service.VoteTaskService;
import com.voting.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VoteTaskServiceImpl implements VoteTaskService {

    @Autowired
    private VoteTaskMapper voteTaskMapper;
    
    @Autowired
    private VoteTaskPartnerMapper voteTaskPartnerMapper;
    
    @Autowired
    private VoteRecordMapper voteRecordMapper;
    
    @Autowired
    private VoteDelegateMapper voteDelegateMapper;
    
    @Autowired
    private ProposalMapper proposalMapper;
    
    @Autowired
    private PartnerMapper partnerMapper;

    /**
     * 根据层级获取票权
     */
    private Integer getLevelWeight(Integer level) {
        switch (level) {
            case 1: return StatusConstant.LEVEL_WEIGHT_1;
            case 2: return StatusConstant.LEVEL_WEIGHT_2;
            case 3: return StatusConstant.LEVEL_WEIGHT_3;
            case 4: return StatusConstant.LEVEL_WEIGHT_4;
            default: return 0;
        }
    }

    /**
     * 创建投票任务
     */
    @Override
    @Transactional
    public void createVoteTask(VoteTaskCreateDTO voteTaskCreateDTO) {
        log.info("创建投票任务：{}", voteTaskCreateDTO);

        // 1. 校验议案状态
        Proposal proposal = proposalMapper.getById(voteTaskCreateDTO.getProposalId());
        if (proposal == null) {
            throw new BaseException(MessageConstant.PROPOSAL_NOT_FOUND);
        }
        if (!StatusConstant.PROPOSAL_PUBLISHED.equals(proposal.getStatus())) {
            throw new BaseException(MessageConstant.PROPOSAL_NOT_PUBLISHED);
        }

        // 2. 检查议案是否已关联投票任务
        VoteTask existTask = voteTaskMapper.getByProposalId(voteTaskCreateDTO.getProposalId());
        if (existTask != null) {
            throw new BaseException(MessageConstant.PROPOSAL_ALREADY_HAS_VOTE_TASK);
        }

        // 3. 校验时间
        if (voteTaskCreateDTO.getEndTime().isBefore(voteTaskCreateDTO.getStartTime())) {
            throw new BaseException(MessageConstant.VOTE_TIME_INVALID);
        }

        // 4. 筛选符合条件的合伙人
        PartnerFilterDTO filter = voteTaskCreateDTO.getPartnerFilter();
        List<Partner> partners = partnerMapper.listByFilter(filter.getLevels(), filter.getStatus());
        
        if (partners == null || partners.isEmpty()) {
            throw new BaseException(MessageConstant.NO_QUALIFIED_PARTNERS);
        }

        // 5. 创建投票任务
        VoteTask voteTask = VoteTask.builder()
                .proposalId(voteTaskCreateDTO.getProposalId())
                .startTime(voteTaskCreateDTO.getStartTime())
                .endTime(voteTaskCreateDTO.getEndTime())
                .voteType(voteTaskCreateDTO.getVoteType())
                .voteStrategy(voteTaskCreateDTO.getVoteStrategy())
                .passRate(voteTaskCreateDTO.getPassRate())
                .status(StatusConstant.VOTE_TASK_IN_PROGRESS)
                .createTime(LocalDateTime.now())
                .creatorId(BaseContext.getCurrentId())
                .build();
        
        voteTaskMapper.insert(voteTask);

        // 6. 批量创建参与人记录
        List<VoteTaskPartner> partnerList = new ArrayList<>();
        for (Partner partner : partners) {
            VoteTaskPartner voteTaskPartner = VoteTaskPartner.builder()
                    .voteTaskId(voteTask.getId())
                    .partnerId(partner.getId())
                    .levelWeight(getLevelWeight(partner.getLevel()))
                    .investRatio(partner.getInvestRatio())
                    .hasVoted(0)
                    .build();
            partnerList.add(voteTaskPartner);
        }
        voteTaskPartnerMapper.batchInsert(partnerList);

        // 7. 更新议案状态为"已关联投票"
        proposalMapper.updateStatus(voteTaskCreateDTO.getProposalId(), StatusConstant.PROPOSAL_LINKED);

        log.info("投票任务创建成功，任务ID：{}，参与人数：{}", voteTask.getId(), partnerList.size());
    }

    /**
     * 投票任务列表分页查询
     */
    @Override
    public PageResult<VoteTaskVO> pageQuery(VoteTaskPageQueryDTO pageQueryDTO) {
        log.info("分页查询投票任务：{}", pageQueryDTO);
        
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<VoteTaskVO> page = (Page<VoteTaskVO>) voteTaskMapper.pageQuery(pageQueryDTO);
        
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * 查询投票任务详情
     */
    @Override
    public VoteTaskDetailVO getDetail(Long id) {
        log.info("查询投票任务详情：{}", id);
        
        VoteTaskDetailVO detail = voteTaskMapper.getDetailById(id);
        if (detail == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }
        
        return detail;
    }

    /**
     * 创建投票任务预览
     */
    @Override
    public VoteTaskPreviewVO preview(VoteTaskCreateDTO voteTaskCreateDTO) {
        log.info("预览投票任务参与人：{}", voteTaskCreateDTO);

        // 筛选符合条件的合伙人
        PartnerFilterDTO filter = voteTaskCreateDTO.getPartnerFilter();
        List<Partner> partners = partnerMapper.listByFilter(filter.getLevels(), filter.getStatus());

        if (partners == null || partners.isEmpty()) {
            return VoteTaskPreviewVO.builder()
                    .totalPartners(0)
                    .totalVotes(0)
                    .totalInvestRatio(BigDecimal.ZERO)
                    .requiredVotes(0)
                    .requiredInvestRatio(BigDecimal.ZERO)
                    .build();
        }

        // 计算总票权和总出资比例
        int totalVotes = 0;
        BigDecimal totalInvestRatio = BigDecimal.ZERO;
        
        for (Partner partner : partners) {
            totalVotes += getLevelWeight(partner.getLevel());
            totalInvestRatio = totalInvestRatio.add(partner.getInvestRatio());
        }

        // 计算通过所需的票数和出资比例
        Integer passRate = voteTaskCreateDTO.getPassRate();
        int requiredVotes = (int) Math.ceil(totalVotes * passRate / 100.0);
        BigDecimal requiredInvestRatio = totalInvestRatio.multiply(new BigDecimal(passRate))
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

        return VoteTaskPreviewVO.builder()
                .totalPartners(partners.size())
                .totalVotes(totalVotes)
                .totalInvestRatio(totalInvestRatio)
                .requiredVotes(requiredVotes)
                .requiredInvestRatio(requiredInvestRatio)
                .build();
    }

    /**
     * 查询投票进度统计
     */
    @Override
    public VoteStatVO getStat(Long id) {
        log.info("查询投票进度统计：{}", id);

        VoteTask voteTask = voteTaskMapper.getById(id);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        // 统计参与人数和已投票人数
        Integer partnerCount = voteTaskPartnerMapper.countByVoteTaskId(id);
        Integer votedCount = voteTaskPartnerMapper.countVotedByVoteTaskId(id);

        // 统计各选项票数
        Integer agreeCount = voteRecordMapper.countByVoteTaskIdAndOption(id, StatusConstant.VOTE_OPTION_AGREE);
        Integer rejectCount = voteRecordMapper.countByVoteTaskIdAndOption(id, StatusConstant.VOTE_OPTION_REJECT);
        Integer abstainCount = voteRecordMapper.countByVoteTaskIdAndOption(id, StatusConstant.VOTE_OPTION_ABSTAIN);

        // 计算进度
        Integer progress = partnerCount > 0 ? (votedCount * 100 / partnerCount) : 0;

        // 计算占比
        BigDecimal agreeRatio = votedCount > 0 ? 
                new BigDecimal(agreeCount).multiply(new BigDecimal(100))
                        .divide(new BigDecimal(votedCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal rejectRatio = votedCount > 0 ? 
                new BigDecimal(rejectCount).multiply(new BigDecimal(100))
                        .divide(new BigDecimal(votedCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal abstainRatio = votedCount > 0 ? 
                new BigDecimal(abstainCount).multiply(new BigDecimal(100))
                        .divide(new BigDecimal(votedCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return VoteStatVO.builder()
                .partnerCount(partnerCount)
                .votedCount(votedCount)
                .progress(progress)
                .agreeCount(agreeCount)
                .rejectCount(rejectCount)
                .abstainCount(abstainCount)
                .agreeRatio(agreeRatio)
                .rejectRatio(rejectRatio)
                .abstainRatio(abstainRatio)
                .build();
    }

    /**
     * 查询投票结果
     */
    @Override
    public VoteResultVO getResult(Long id) {
        log.info("计算投票结果：{}", id);

        VoteTask voteTask = voteTaskMapper.getById(id);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        Integer voteStrategy = voteTask.getVoteStrategy();
        Integer passRate = voteTask.getPassRate();

        VoteResultVO result = VoteResultVO.builder()
                .voteTaskId(id)
                .voteStrategy(voteStrategy)
                .passRate(passRate)
                .build();

        // 获取所有投票记录
        List<VoteRecord> records = voteRecordMapper.listByVoteTaskId(id);
        
        // 创建映射：partner_id -> vote_option
        Map<Long, Integer> voteMap = new HashMap<>();
        for (VoteRecord record : records) {
            voteMap.put(record.getPartnerId(), record.getVoteOption());
        }

        // 获取所有参与人
        List<VoteTaskPartnerVO> partners = voteTaskPartnerMapper.listByVoteTaskId(id);

        // 计算人数票
        int peopleAgreeVotes = 0, peopleRejectVotes = 0, peopleAbstainVotes = 0;
        BigDecimal capitalAgreeRatio = BigDecimal.ZERO;
        BigDecimal capitalRejectRatio = BigDecimal.ZERO;
        BigDecimal capitalAbstainRatio = BigDecimal.ZERO;

        for (VoteTaskPartnerVO partner : partners) {
            if (partner.getHasVoted() == 0) continue;  // 未投票的不计入

            Integer voteOption = voteMap.get(partner.getPartnerId());
            if (voteOption == null) continue;

            // 人数票计算
            if (voteOption.equals(StatusConstant.VOTE_OPTION_AGREE)) {
                peopleAgreeVotes += partner.getLevelWeight();
                capitalAgreeRatio = capitalAgreeRatio.add(partner.getInvestRatio());
            } else if (voteOption.equals(StatusConstant.VOTE_OPTION_REJECT)) {
                peopleRejectVotes += partner.getLevelWeight();
                capitalRejectRatio = capitalRejectRatio.add(partner.getInvestRatio());
            } else if (voteOption.equals(StatusConstant.VOTE_OPTION_ABSTAIN)) {
                peopleAbstainVotes += partner.getLevelWeight();
                capitalAbstainRatio = capitalAbstainRatio.add(partner.getInvestRatio());
            }
        }

        int peopleTotalVotes = peopleAgreeVotes + peopleRejectVotes + peopleAbstainVotes;
        BigDecimal capitalTotalRatio = capitalAgreeRatio.add(capitalRejectRatio).add(capitalAbstainRatio);

        // 计算得票率
        BigDecimal peopleAgreeRatio = peopleTotalVotes > 0 ? 
                new BigDecimal(peopleAgreeVotes).multiply(new BigDecimal(100))
                        .divide(new BigDecimal(peopleTotalVotes), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        BigDecimal capitalAgreeVoteRatio = capitalTotalRatio.compareTo(BigDecimal.ZERO) > 0 ? 
                capitalAgreeRatio.multiply(new BigDecimal(100))
                        .divide(capitalTotalRatio, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 判断是否通过
        boolean peoplePassed = peopleAgreeRatio.compareTo(new BigDecimal(passRate)) >= 0;
        boolean capitalPassed = capitalAgreeVoteRatio.compareTo(new BigDecimal(passRate)) >= 0;

        // 最终结果
        boolean finalResult;
        String resultDesc;
        
        if (voteStrategy.equals(StatusConstant.VOTE_STRATEGY_PEOPLE)) {
            finalResult = peoplePassed;
            resultDesc = String.format("人数票：同意得票率%.2f%%，%s（阈值%d%%）",
                    peopleAgreeRatio, peoplePassed ? "通过" : "未通过", passRate);
        } else if (voteStrategy.equals(StatusConstant.VOTE_STRATEGY_CAPITAL)) {
            finalResult = capitalPassed;
            resultDesc = String.format("出资票：同意得票率%.2f%%，%s（阈值%d%%）",
                    capitalAgreeVoteRatio, capitalPassed ? "通过" : "未通过", passRate);
        } else {  // 组合策略
            finalResult = peoplePassed && capitalPassed;
            resultDesc = String.format("组合策略：人数票%.2f%%%s，出资票%.2f%%%s，最终%s（阈值%d%%）",
                    peopleAgreeRatio, peoplePassed ? "通过" : "未通过",
                    capitalAgreeVoteRatio, capitalPassed ? "通过" : "未通过",
                    finalResult ? "通过" : "未通过", passRate);
        }

        result.setPeopleAgreeVotes(peopleAgreeVotes);
        result.setPeopleRejectVotes(peopleRejectVotes);
        result.setPeopleAbstainVotes(peopleAbstainVotes);
        result.setPeopleTotalVotes(peopleTotalVotes);
        result.setPeopleAgreeRatio(peopleAgreeRatio);
        result.setPeoplePassed(peoplePassed);
        
        result.setCapitalAgreeRatio(capitalAgreeRatio);
        result.setCapitalRejectRatio(capitalRejectRatio);
        result.setCapitalAbstainRatio(capitalAbstainRatio);
        result.setCapitalTotalRatio(capitalTotalRatio);
        result.setCapitalAgreeVoteRatio(capitalAgreeVoteRatio);
        result.setCapitalPassed(capitalPassed);
        
        result.setFinalResult(finalResult);
        result.setResultDesc(resultDesc);

        return result;
    }

    /**
     * 提前结束投票
     */
    @Override
    @Transactional
    public void endVoteTask(Long id) {
        log.info("提前结束投票任务：{}", id);

        VoteTask voteTask = voteTaskMapper.getById(id);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        if (StatusConstant.VOTE_TASK_ENDED.equals(voteTask.getStatus())) {
            throw new BaseException(MessageConstant.VOTE_TASK_ALREADY_ENDED);
        }

        // 更新投票任务状态
        voteTaskMapper.updateStatus(id, StatusConstant.VOTE_TASK_ENDED);

        // 更新议案状态为"已结束"
        proposalMapper.updateStatus(voteTask.getProposalId(), StatusConstant.PROPOSAL_ENDED);

        log.info("投票任务已结束：{}", id);
    }

    /**
     * 查询投票任务参与人列表
     */
    @Override
    public Page<VoteTaskPartnerVO> getPartners(Long id, Integer page, Integer pageSize) {
        log.info("查询投票任务参与人列表：{}", id);

        VoteTask voteTask = voteTaskMapper.getById(id);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        PageHelper.startPage(page, pageSize);
        List<VoteTaskPartnerVO> list = voteTaskPartnerMapper.listByVoteTaskId(id);
        
        return (Page<VoteTaskPartnerVO>) list;
    }
}
