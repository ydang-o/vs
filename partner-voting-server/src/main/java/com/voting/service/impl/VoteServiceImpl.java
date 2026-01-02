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
import com.voting.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    @Autowired
    private ProposalMapper proposalMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

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

    // ==================== H5接口实现 ====================

    /**
     * H5待投票任务列表
     */
    @Override
    public Page<H5VoteTaskVO> getH5TodoList(Integer page, Integer pageSize) {
        Long partnerId = getCurrentPartnerId();
        log.info("H5查询待投票列表，合伙人ID：{}", partnerId);

        PageHelper.startPage(page, pageSize);
        List<MyVoteTaskVO> list = voteTaskPartnerMapper.pageMyPending(partnerId);

        // 转换为H5VO并计算urgent标识
        List<H5VoteTaskVO> h5List = list.stream().map(vo -> {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, vo.getEndTime());
            boolean urgent = duration.toHours() < 24 && duration.toHours() >= 0;

            return H5VoteTaskVO.builder()
                    .voteTaskId(vo.getId())
                    .proposalTitle(vo.getProposalTitle())
                    .endTime(vo.getEndTime())
                    .urgent(urgent)
                    .build();
        }).collect(Collectors.toList());

        Page<H5VoteTaskVO> result = new Page<>();
        result.addAll(h5List);
        result.setTotal(((Page<MyVoteTaskVO>) list).getTotal());

        return result;
    }

    /**
     * H5投票任务详情
     */
    @Override
    public H5VoteTaskDetailVO getH5TaskDetail(Long voteTaskId) {
        Long partnerId = getCurrentPartnerId();
        log.info("H5查询投票任务详情，任务ID：{}，合伙人ID：{}", voteTaskId, partnerId);

        // 1. 查询投票任务
        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        // 2. 查询议案
        Proposal proposal = proposalMapper.getById(voteTask.getProposalId());

        // 3. 查询当前合伙人的参与记录
        VoteTaskPartner taskPartner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, partnerId);
        boolean canVote = taskPartner != null;
        boolean hasVoted = canVote && taskPartner.getHasVoted() == 1;

        // 4. 查询是否有人委托给当前合伙人
        List<VoteDelegate> delegateList = voteDelegateMapper.listByVoteTaskIdAndToPartnerId(voteTaskId, partnerId);
        boolean canDelegateVote = !delegateList.isEmpty();

        return H5VoteTaskDetailVO.builder()
                .title(proposal.getTitle())
                .content(proposal.getContent())
                .voteTaskId(voteTaskId)
                .endTime(voteTask.getEndTime())
                .voteType(voteTask.getVoteType())
                .hasVoted(hasVoted)
                .canVote(canVote)
                .canDelegateVote(canDelegateVote)
                .build();
    }

    /**
     * H5委托投票详情查询
     */
    @Override
    public DelegateDetailVO getDelegateDetail(Long voteTaskId) {
        Long toPartnerId = getCurrentPartnerId();
        log.info("H5查询委托详情，任务ID：{}，被委托人ID：{}", voteTaskId, toPartnerId);

        // 查询委托给当前合伙人的记录（只取第一条有效的）
        List<VoteDelegate> delegateList = voteDelegateMapper.listByVoteTaskIdAndToPartnerId(voteTaskId, toPartnerId);
        
        if (delegateList.isEmpty()) {
            throw new BaseException("无委托记录");
        }

        VoteDelegate delegate = delegateList.get(0);
        
        // 查询委托人信息
        Partner fromPartner = partnerMapper.getById(delegate.getFromPartnerId());
        SysUser fromUser = sysUserMapper.getById(fromPartner.getUserId());

        // 查询委托人是否已投票
        VoteTaskPartner fromTaskPartner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, delegate.getFromPartnerId());
        boolean hasVoted = fromTaskPartner != null && fromTaskPartner.getHasVoted() == 1;

        return DelegateDetailVO.builder()
                .fromPartnerId(delegate.getFromPartnerId())
                .fromPartnerName(fromUser.getRealName())
                .hasVoted(hasVoted)
                .build();
    }

    /**
     * H5提交委托投票
     */
    @Override
    @Transactional
    public void submitDelegateVote(Long voteTaskId, Long fromPartnerId, Integer voteOption) {
        Long toPartnerId = getCurrentPartnerId();
        log.info("H5提交委托投票，任务ID：{}，委托人ID：{}，被委托人ID：{}，选项：{}", 
                voteTaskId, fromPartnerId, toPartnerId, voteOption);

        // 1. 校验投票任务
        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }
        if (!StatusConstant.VOTE_TASK_IN_PROGRESS.equals(voteTask.getStatus())) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_IN_PROGRESS);
        }

        // 2. 校验委托关系
        VoteDelegate delegate = voteDelegateMapper.getByVoteTaskIdAndFromPartnerId(voteTaskId, fromPartnerId);
        if (delegate == null || !StatusConstant.ENABLE.equals(delegate.getStatus())) {
            throw new BaseException("委托记录不存在或已失效");
        }
        if (!delegate.getToPartnerId().equals(toPartnerId)) {
            throw new BaseException("您不是该委托的被委托人");
        }

        // 3. 校验委托人是否已投票
        VoteTaskPartner fromTaskPartner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, fromPartnerId);
        if (fromTaskPartner == null) {
            throw new BaseException(MessageConstant.NOT_VOTE_PARTICIPANT);
        }
        if (fromTaskPartner.getHasVoted() == 1) {
            throw new BaseException("委托人已投票，无法代投");
        }

        // 4. 创建投票记录（注意：partner_id是委托人，delegate_partner_id是被委托人）
        VoteRecord voteRecord = VoteRecord.builder()
                .voteTaskId(voteTaskId)
                .partnerId(fromPartnerId)  // 投票人是委托人
                .voteOption(voteOption)
                .voteTime(LocalDateTime.now())
                .isDelegate(1)  // 标记为委托投票
                .delegatePartnerId(toPartnerId)  // 被委托人
                .build();
        voteRecordMapper.insert(voteRecord);

        // 5. 更新委托人的投票状态
        voteTaskPartnerMapper.updateHasVoted(voteTaskId, fromPartnerId);

        log.info("委托投票成功，记录ID：{}", voteRecord.getId());
    }

    /**
     * H5已投票列表
     */
    @Override
    public Page<MyVoteTaskVO> getH5DoneList(Integer page, Integer pageSize) {
        // 复用已投票列表接口
        return getMyVotedList(page, pageSize);
    }

    // ==================== 结果统计接口实现 ====================

    /**
     * 投票结果汇总
     */
    @Override
    public VoteSummaryVO getVoteSummary(Long voteTaskId) {
        log.info("查询投票结果汇总，任务ID：{}", voteTaskId);

        // 1. 查询投票任务
        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        // 2. 查询议案
        Proposal proposal = proposalMapper.getById(voteTask.getProposalId());

        // 3. 查询投票记录
        List<VoteRecord> records = voteRecordMapper.listByVoteTaskId(voteTaskId);
        
        // 4. 查询所有参与人
        List<VoteTaskPartner> partners = new ArrayList<>();
        for (VoteRecord record : records) {
            VoteTaskPartner partner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, record.getPartnerId());
            if (partner != null) {
                partners.add(partner);
            }
        }

        VoteSummaryVO.PeopleVoteResult peopleVote = null;
        VoteSummaryVO.CapitalVoteResult capitalVote = null;
        String finalResult = "";

        // 5. 根据投票策略计算结果
        if (StatusConstant.VOTE_STRATEGY_PEOPLE.equals(voteTask.getVoteStrategy()) || 
            StatusConstant.VOTE_STRATEGY_COMBINED.equals(voteTask.getVoteStrategy())) {
            // 人数票计算
            int totalWeight = 0;
            int agreeWeight = 0;
            int rejectWeight = 0;
            int abstainWeight = 0;

            for (VoteRecord record : records) {
                VoteTaskPartner partner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, record.getPartnerId());
                if (partner != null) {
                    int weight = partner.getLevelWeight();
                    totalWeight += weight;
                    
                    if (StatusConstant.VOTE_OPTION_AGREE.equals(record.getVoteOption())) {
                        agreeWeight += weight;
                    } else if (StatusConstant.VOTE_OPTION_REJECT.equals(record.getVoteOption())) {
                        rejectWeight += weight;
                    } else if (StatusConstant.VOTE_OPTION_ABSTAIN.equals(record.getVoteOption())) {
                        abstainWeight += weight;
                    }
                }
            }

            String agreeRate = "0%";
            boolean pass = false;
            if (totalWeight > 0) {
                BigDecimal rate = new BigDecimal(agreeWeight)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalWeight), 2, RoundingMode.HALF_UP);
                agreeRate = rate.intValue() + "%";
                pass = rate.compareTo(new BigDecimal(voteTask.getPassRate())) >= 0;
            }

            peopleVote = VoteSummaryVO.PeopleVoteResult.builder()
                    .agree(agreeWeight)
                    .reject(rejectWeight)
                    .abstain(abstainWeight)
                    .agreeRate(agreeRate)
                    .pass(pass)
                    .build();
        }

        if (StatusConstant.VOTE_STRATEGY_CAPITAL.equals(voteTask.getVoteStrategy()) || 
            StatusConstant.VOTE_STRATEGY_COMBINED.equals(voteTask.getVoteStrategy())) {
            // 出资票计算
            BigDecimal totalRatio = BigDecimal.ZERO;
            BigDecimal agreeRatio = BigDecimal.ZERO;

            for (VoteRecord record : records) {
                VoteTaskPartner partner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, record.getPartnerId());
                if (partner != null) {
                    totalRatio = totalRatio.add(partner.getInvestRatio());
                    
                    if (StatusConstant.VOTE_OPTION_AGREE.equals(record.getVoteOption())) {
                        agreeRatio = agreeRatio.add(partner.getInvestRatio());
                    }
                }
            }

            String agreeRate = "0%";
            boolean pass = false;
            if (totalRatio.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal rate = agreeRatio.multiply(new BigDecimal(100))
                        .divide(totalRatio, 2, RoundingMode.HALF_UP);
                agreeRate = rate.intValue() + "%";
                pass = rate.compareTo(new BigDecimal(voteTask.getPassRate())) >= 0;
            }

            capitalVote = VoteSummaryVO.CapitalVoteResult.builder()
                    .agreeRate(agreeRate)
                    .pass(pass)
                    .build();
        }

        // 6. 计算最终结果
        if (StatusConstant.VOTE_STRATEGY_COMBINED.equals(voteTask.getVoteStrategy())) {
            // 组合策略：两者都通过才算通过
            finalResult = (peopleVote.getPass() && capitalVote.getPass()) ? "通过" : "未通过";
        } else if (StatusConstant.VOTE_STRATEGY_PEOPLE.equals(voteTask.getVoteStrategy())) {
            finalResult = peopleVote.getPass() ? "通过" : "未通过";
        } else if (StatusConstant.VOTE_STRATEGY_CAPITAL.equals(voteTask.getVoteStrategy())) {
            finalResult = capitalVote.getPass() ? "通过" : "未通过";
        }

        return VoteSummaryVO.builder()
                .voteTaskId(voteTaskId)
                .proposalTitle(proposal.getTitle())
                .voteStrategy(voteTask.getVoteStrategy())
                .passRate(voteTask.getPassRate())
                .peopleVote(peopleVote)
                .capitalVote(capitalVote)
                .finalResult(finalResult)
                .build();
    }

    /**
     * 投票明细查询（实名模式专用）
     */
    @Override
    public List<VoteDetailRecordVO> getVoteDetail(Long voteTaskId) {
        log.info("查询投票明细，任务ID：{}", voteTaskId);

        // 1. 校验投票任务
        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }

        // 2. 仅实名模式允许查看明细
        if (!StatusConstant.VOTE_TYPE_REAL_NAME.equals(voteTask.getVoteType())) {
            throw new BaseException("匿名投票不允许查看明细");
        }

        // 3. 查询投票记录
        List<VoteRecord> records = voteRecordMapper.listDetailByVoteTaskId(voteTaskId);

        // 4. 转换为VO
        List<VoteDetailRecordVO> result = new ArrayList<>();
        for (VoteRecord record : records) {
            // 查询投票人信息
            Partner partner = partnerMapper.getById(record.getPartnerId());
            SysUser user = sysUserMapper.getById(partner.getUserId());

            String voteOptionText = "";
            if (StatusConstant.VOTE_OPTION_AGREE.equals(record.getVoteOption())) {
                voteOptionText = "同意";
            } else if (StatusConstant.VOTE_OPTION_REJECT.equals(record.getVoteOption())) {
                voteOptionText = "反对";
            } else if (StatusConstant.VOTE_OPTION_ABSTAIN.equals(record.getVoteOption())) {
                voteOptionText = "弃权";
            }

            String delegateByName = null;
            if (record.getIsDelegate() == 1 && record.getDelegatePartnerId() != null) {
                Partner delegatePartner = partnerMapper.getById(record.getDelegatePartnerId());
                SysUser delegateUser = sysUserMapper.getById(delegatePartner.getUserId());
                delegateByName = delegateUser.getRealName();
            }

            VoteDetailRecordVO vo = VoteDetailRecordVO.builder()
                    .partnerName(user.getRealName())
                    .voteOption(voteOptionText)
                    .voteTime(record.getVoteTime())
                    .isDelegate(record.getIsDelegate() == 1)
                    .delegateBy(delegateByName)
                    .build();

            result.add(vo);
        }

        return result;
    }

    /**
     * 导出投票结果Excel
     */
    @Override
    public void exportVoteResult(Long voteTaskId, HttpServletResponse response) {
        log.info("导出投票结果，任务ID：{}", voteTaskId);

        VoteTask voteTask = voteTaskMapper.getById(voteTaskId);
        if (voteTask == null) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_FOUND);
        }
        if (!StatusConstant.VOTE_TASK_ENDED.equals(voteTask.getStatus())) {
            throw new BaseException(MessageConstant.VOTE_TASK_NOT_ENDED);
        }

        Proposal proposal = proposalMapper.getById(voteTask.getProposalId());
        int partnerCount = voteTaskPartnerMapper.countByVoteTaskId(voteTaskId);

        List<VoteRecord> records = voteRecordMapper.listByVoteTaskId(voteTaskId);
        int agreeCount = 0, rejectCount = 0, abstainCount = 0;
        BigDecimal totalCapital = BigDecimal.ZERO;
        BigDecimal agreeCapital = BigDecimal.ZERO;
        BigDecimal rejectCapital = BigDecimal.ZERO;
        BigDecimal abstainCapital = BigDecimal.ZERO;

        for (VoteRecord record : records) {
            if (StatusConstant.VOTE_OPTION_AGREE.equals(record.getVoteOption())) {
                agreeCount++;
            } else if (StatusConstant.VOTE_OPTION_REJECT.equals(record.getVoteOption())) {
                rejectCount++;
            } else if (StatusConstant.VOTE_OPTION_ABSTAIN.equals(record.getVoteOption())) {
                abstainCount++;
            }

            VoteTaskPartner partner = voteTaskPartnerMapper.getByVoteTaskIdAndPartnerId(voteTaskId, record.getPartnerId());
            if (partner != null && partner.getInvestRatio() != null) {
                totalCapital = totalCapital.add(partner.getInvestRatio());
                if (StatusConstant.VOTE_OPTION_AGREE.equals(record.getVoteOption())) {
                    agreeCapital = agreeCapital.add(partner.getInvestRatio());
                } else if (StatusConstant.VOTE_OPTION_REJECT.equals(record.getVoteOption())) {
                    rejectCapital = rejectCapital.add(partner.getInvestRatio());
                } else if (StatusConstant.VOTE_OPTION_ABSTAIN.equals(record.getVoteOption())) {
                    abstainCapital = abstainCapital.add(partner.getInvestRatio());
                }
            }
        }

        VoteSummaryVO summary = getVoteSummary(voteTaskId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String watermark = "合伙人投票系统 · 导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Sheet 1: 投票任务信息
            Sheet infoSheet = workbook.createSheet("投票任务信息");
            infoSheet.getHeader().setCenter(watermark);
            CellStyle boldStyle = createBoldCellStyle(workbook);

            createInfoRow(infoSheet, 0, "议案名称", proposal.getTitle(), boldStyle);
            createInfoRow(infoSheet, 1, "投票策略", strategyText(voteTask.getVoteStrategy()), boldStyle);
            createInfoRow(infoSheet, 2, "起止时间", formatDateTime(voteTask.getStartTime()) + " ~ " + formatDateTime(voteTask.getEndTime()), boldStyle);
            createInfoRow(infoSheet, 3, "参与人数", String.valueOf(partnerCount), boldStyle);
            createInfoRow(infoSheet, 4, "最终结果", summary.getFinalResult(), boldStyle);
            autoSizeColumns(infoSheet, 2);

            // Sheet 2: 投票结果汇总
            Sheet summarySheet = workbook.createSheet("投票结果汇总");
            summarySheet.getHeader().setCenter(watermark);
            int rowIdx = 0;

            if (StatusConstant.VOTE_STRATEGY_PEOPLE.equals(voteTask.getVoteStrategy()) ||
                StatusConstant.VOTE_STRATEGY_COMBINED.equals(voteTask.getVoteStrategy())) {
                rowIdx = writePeopleSummary(summarySheet, rowIdx, agreeCount, rejectCount, abstainCount, records.size(), boldStyle);
            }

            if (StatusConstant.VOTE_STRATEGY_CAPITAL.equals(voteTask.getVoteStrategy()) ||
                StatusConstant.VOTE_STRATEGY_COMBINED.equals(voteTask.getVoteStrategy())) {
                rowIdx += 1; // 空一行
                writeCapitalSummary(summarySheet, rowIdx, agreeCapital, rejectCapital, abstainCapital, totalCapital, boldStyle);
            }
            autoSizeColumns(summarySheet, 4);

            // Sheet 3: 投票明细（实名模式）
            if (StatusConstant.VOTE_TYPE_REAL_NAME.equals(voteTask.getVoteType())) {
                Sheet detailSheet = workbook.createSheet("投票明细");
                detailSheet.getHeader().setCenter(watermark);
                writeDetailSheet(detailSheet, records, voteTaskId, boldStyle);
                autoSizeColumns(detailSheet, 5);
            }

            // 设置响应并输出
            String dateStr = voteTask.getEndTime() != null ? voteTask.getEndTime().format(DateTimeFormatter.ofPattern("yyyyMMdd")) : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = "投票结果_" + proposal.getTitle() + "_" + dateStr + ".xlsx";
            String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodeName);

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("导出投票结果失败", e);
            throw new BaseException("导出失败");
        }
    }

    private void writeDetailSheet(Sheet sheet, List<VoteRecord> records, Long voteTaskId, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        String[] headers = {"投票人", "投票选项", "是否委托", "被委托人", "投票时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Map<Long, String> nameCache = new HashMap<>();

        int rowNum = 1;
        for (VoteRecord record : records) {
            Row row = sheet.createRow(rowNum++);

            String partnerName = getPartnerName(record.getPartnerId(), nameCache);
            String optionText = optionText(record.getVoteOption());
            boolean delegated = record.getIsDelegate() != null && record.getIsDelegate() == 1;
            String delegateName = delegated && record.getDelegatePartnerId() != null ? getPartnerName(record.getDelegatePartnerId(), nameCache) : "-";

            row.createCell(0).setCellValue(partnerName);
            row.createCell(1).setCellValue(optionText);
            row.createCell(2).setCellValue(delegated ? "是" : "否");
            row.createCell(3).setCellValue(delegateName);
            row.createCell(4).setCellValue(formatDateTime(record.getVoteTime()));
        }
    }

    private int writePeopleSummary(Sheet sheet, int startRow, int agreeCount, int rejectCount, int abstainCount, int total, CellStyle headerStyle) {
        Row titleRow = sheet.createRow(startRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("人数票汇总");
        titleCell.setCellStyle(headerStyle);

        Row header = sheet.createRow(startRow++);
        String[] headers = {"选项", "数量", "占比"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        startRow = writeSummaryRow(sheet, startRow, "同意", agreeCount, total);
        startRow = writeSummaryRow(sheet, startRow, "反对", rejectCount, total);
        startRow = writeSummaryRow(sheet, startRow, "弃权", abstainCount, total);

        return startRow;
    }

    private void writeCapitalSummary(Sheet sheet, int startRow, BigDecimal agree, BigDecimal reject, BigDecimal abstain, BigDecimal total, CellStyle headerStyle) {
        Row titleRow = sheet.createRow(startRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("出资票汇总");
        titleCell.setCellStyle(headerStyle);

        Row header = sheet.createRow(startRow++);
        String[] headers = {"选项", "权重占比"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        writeCapitalRow(sheet, startRow++, "同意", agree, total);
        writeCapitalRow(sheet, startRow++, "反对", reject, total);
        writeCapitalRow(sheet, startRow, "弃权", abstain, total);
    }

    private int writeSummaryRow(Sheet sheet, int rowIdx, String option, int count, int total) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(option);
        row.createCell(1).setCellValue(count);
        row.createCell(2).setCellValue(formatPercent(count, total));
        return rowIdx;
    }

    private void writeCapitalRow(Sheet sheet, int rowIdx, String option, BigDecimal value, BigDecimal total) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(option);
        row.createCell(1).setCellValue(formatPercent(value, total));
    }

    private String optionText(Integer voteOption) {
        if (StatusConstant.VOTE_OPTION_AGREE.equals(voteOption)) {
            return "同意";
        } else if (StatusConstant.VOTE_OPTION_REJECT.equals(voteOption)) {
            return "反对";
        } else if (StatusConstant.VOTE_OPTION_ABSTAIN.equals(voteOption)) {
            return "弃权";
        }
        return "-";
    }

    private String getPartnerName(Long partnerId, Map<Long, String> cache) {
        if (cache.containsKey(partnerId)) {
            return cache.get(partnerId);
        }
        Partner partner = partnerMapper.getById(partnerId);
        SysUser user = partner != null ? sysUserMapper.getById(partner.getUserId()) : null;
        String name = user != null ? user.getRealName() : "-";
        cache.put(partnerId, name);
        return name;
    }

    private String strategyText(Integer strategy) {
        if (StatusConstant.VOTE_STRATEGY_PEOPLE.equals(strategy)) {
            return "人数票";
        } else if (StatusConstant.VOTE_STRATEGY_CAPITAL.equals(strategy)) {
            return "出资票";
        } else if (StatusConstant.VOTE_STRATEGY_COMBINED.equals(strategy)) {
            return "人数票 + 出资票";
        }
        return "-";
    }

    private String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formatPercent(int count, int total) {
        if (total <= 0) {
            return "0%";
        }
        return BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP)
                .intValue() + "%";
    }

    private String formatPercent(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return "0%";
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue() + "%";
    }

    private CellStyle createBoldCellStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private void createInfoRow(Sheet sheet, int rowIdx, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(value);
    }

    private void autoSizeColumns(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}

