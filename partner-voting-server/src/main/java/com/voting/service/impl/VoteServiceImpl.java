package com.voting.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.voting.constant.MessageConstant;
import com.voting.constant.StatusConstant;
import com.voting.context.BaseContext;
import com.voting.dto.DelegateCreateDTO;
import com.voting.exception.BaseException;
import com.voting.dto.DelegatePageQueryDTO;
import com.voting.entity.*;
import com.voting.mapper.*;
import com.voting.result.PageResult;
import com.voting.service.VoteService;
import com.voting.vo.DelegateVO;
import com.voting.vo.MyVoteTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class VoteServiceImpl implements VoteService {

    @Autowired
    private VoteTaskMapper voteTaskMapper;
    
    @Autowired
    private VoteTaskPartnerMapper voteTaskPartnerMapper;
    
    @Autowired
    private VoteRecordMapper voteRecordMapper;
    
    @Autowired
    private VoteDelegateMapper voteDelegateMapper;
    
    @Autowired
    private PartnerMapper partnerMapper;

    /**
     * 获取当前用户的合伙人ID
     */
    private Long getCurrentPartnerId() {
        Long userId = BaseContext.getCurrentId();
        Partner partner = partnerMapper.getByUserId(userId);
        if (partner == null) {
            throw new BaseException(MessageConstant.PARTNER_NOT_FOUND);
        }
        return partner.getId();
    }

    /**
     * 我的待投票列表
     */
    @Override
    public Page<MyVoteTaskVO> getMyPendingVotes(Integer page, Integer pageSize) {
        Long partnerId = getCurrentPartnerId();
        log.info("查询待投票列表，合伙人ID：{}", partnerId);
        PageHelper.startPage(page, pageSize);
        List<MyVoteTaskVO> list = voteTaskPartnerMapper.pageMyPending(partnerId);
        return (Page<MyVoteTaskVO>) list;
    }

    /**
     * 我的已投票列表
     */
    @Override
    public Page<MyVoteTaskVO> getMyVotedList(Integer page, Integer pageSize) {
        Long partnerId = getCurrentPartnerId();
        log.info("查询已投票列表，合伙人ID：{}", partnerId);
        PageHelper.startPage(page, pageSize);
        List<MyVoteTaskVO> list = voteTaskPartnerMapper.pageMyVoted(partnerId);
        return (Page<MyVoteTaskVO>) list;
    }

    /**
     * 提交投票
     */
    @Override
    @Transactional
    public void submitVote(Long voteTaskId, Integer voteOption) {
        Long partnerId = getCurrentPartnerId();
        log.info("提交投票，任务ID：{}，合伙人ID：{}，选项：{}", voteTaskId, partnerId, voteOption);

        // 1. 校验投票任务
        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        if (!StatusConstant.VOTE_TASK_IN_PROGRESS.equals(voteTask.getStatus())) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_IN_PROGRESS);
        }

        // 2. 校验是否为参与人
        VoteTaskPartner taskPartner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, partnerId);
        if (taskPartner == null) {
            throw new BaseException(MessageConstant.NOT_VOTE_PARTICIPANT);
        }

        // 3. 校验是否已投票
        if (taskPartner.getHasVoted() == 1) {
            throw new BaseException(MessageConstant.ALREADY_VOTED);
        }

        // 4. 校验是否已委托他人
        VoteDelegate delegate = voteDelegateMapper.getByVoteTaskIdAndFromPartnerId(voteTaskId, partnerId);
        if (delegate != null && StatusConstant.ENABLE.equals(delegate.getStatus())) {
            throw new BaseException(MessageConstant.HAS_DELEGATED);
        }

        // 5. 创建投票记录
        VoteRecord voteRecord = VoteRecord.builder()
                .voteTaskId(voteTaskId)
                .partnerId(partnerId)
                .voteOption(voteOption)
                .voteTime(LocalDateTime.now())
                .isDelegate(0)
                .build();
        voteRecordMapper.insert(voteRecord);

        // 6. 更新参与人投票状态
        voteTaskPartnerMapper.updateHasVoted(voteTaskId, partnerId);

        log.info("投票成功，记录ID：{}", voteRecord.getId());
    }

    /**
     * 创建委托
     */
    @Override
    @Transactional
    public void createDelegate(DelegateCreateDTO delegateCreateDTO) {
        log.info("创建委托：{}", delegateCreateDTO);

        Long voteTaskId = delegateCreateDTO.getVoteTaskId();
        Long fromPartnerId = delegateCreateDTO.getFromPartnerId();
        Long toPartnerId = delegateCreateDTO.getToPartnerId();

        // 1. 校验投票任务
        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        if (!StatusConstant.VOTE_TASK_IN_PROGRESS.equals(voteTask.getStatus())) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_IN_PROGRESS);
        }

        // 2. 校验委托人是否为参与人
        VoteTaskPartner fromPartner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, fromPartnerId);
        if (fromPartner == null) {
            throw new BaseException(MessageConstant.NOT_VOTE_PARTICIPANT);
        }

        // 3. 校验委托人是否已投票
        if (fromPartner.getHasVoted() == 1) {
            throw new BaseException(MessageConstant.ALREADY_VOTED);
        }

        // 4. 校验是否已有委托记录
        VoteDelegate existDelegate = voteDelegateMapper.getByVoteTaskIdAndFromPartnerId(voteTaskId, fromPartnerId);
        if (existDelegate != null && StatusConstant.ENABLE.equals(existDelegate.getStatus())) {
            throw new BaseException(MessageConstant.DELEGATE_ALREADY_EXISTS);
        }

        // 5. 校验不能委托给自己
        if (fromPartnerId.equals(toPartnerId)) {
            throw new BaseException(MessageConstant.CANNOT_DELEGATE_TO_SELF);
        }

        // 6. 校验被委托人是否为参与人
        VoteTaskPartner toPartner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, toPartnerId);
        if (toPartner == null) {
            throw new BaseException(MessageConstant.DELEGATE_TARGET_NOT_PARTICIPANT);
        }

        // 7. 创建委托记录
        VoteDelegate delegate = VoteDelegate.builder()
                .voteTaskId(voteTaskId)
                .fromPartnerId(fromPartnerId)
                .toPartnerId(toPartnerId)
                .proofFile(delegateCreateDTO.getProofFile())
                .status(StatusConstant.ENABLE)
                .createTime(LocalDateTime.now())
                .build();
        voteDelegateMapper.insert(delegate);

        log.info("委托创建成功，委托ID：{}", delegate.getId());
    }

    /**
     * 委托管理列表
     */
    @Override
    public PageResult<DelegateVO> getDelegateList(DelegatePageQueryDTO pageQueryDTO) {
        log.info("查询委托列表：{}", pageQueryDTO);

        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<DelegateVO> page = (Page<DelegateVO>) voteDelegateMapper.pageQuery(pageQueryDTO);

        return new PageResult<>(page.getTotal(), page.getResult());
    }
}
