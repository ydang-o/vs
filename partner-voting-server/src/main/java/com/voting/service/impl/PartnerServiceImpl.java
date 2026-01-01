package com.voting.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.voting.constant.MessageConstant;
import com.voting.constant.StatusConstant;
import com.voting.dto.PartnerAddDTO;
import com.voting.dto.PartnerPageQueryDTO;
import com.voting.dto.PartnerUpdateDTO;
import com.voting.entity.Partner;
import com.voting.entity.SysUser;
import com.voting.exception.BaseException;
import com.voting.mapper.PartnerMapper;
import com.voting.mapper.SysUserMapper;
import com.voting.result.PageResult;
import com.voting.service.PartnerService;
import com.voting.vo.PartnerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合伙人服务实现类
 */
@Service
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    @Autowired
    private PartnerMapper partnerMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 新增合伙人（用户关联）
     * @param partnerAddDTO 合伙人信息
     */
    @Override
    public void add(PartnerAddDTO partnerAddDTO) {
        log.info("新增合伙人：{}", partnerAddDTO);

        // 校验用户是否存在
        SysUser sysUser = sysUserMapper.getById(partnerAddDTO.getUserId());
        if (sysUser == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        // 校验用户是否已关联合伙人
        int count = partnerMapper.countByUserId(partnerAddDTO.getUserId());
        if (count > 0) {
            throw new BaseException(MessageConstant.PARTNER_USER_ALREADY_BOUND);
        }

        // 创建合伙人
        Partner partner = new Partner();
        BeanUtils.copyProperties(partnerAddDTO, partner);
        partner.setStatus(StatusConstant.ENABLE);
        partner.setBindTime(LocalDateTime.now());

        partnerMapper.insert(partner);
    }

    /**
     * 合伙人列表分页查询
     * @param partnerPageQueryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(PartnerPageQueryDTO partnerPageQueryDTO) {
        log.info("合伙人列表查询：{}", partnerPageQueryDTO);

        PageHelper.startPage(partnerPageQueryDTO.getPageNum(), partnerPageQueryDTO.getPageSize());
        Page<PartnerVO> page = partnerMapper.pageQuery(partnerPageQueryDTO);

        long total = page.getTotal();
        List<PartnerVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 编辑合伙人信息
     * @param partnerUpdateDTO 合伙人信息
     */
    @Override
    public void update(PartnerUpdateDTO partnerUpdateDTO) {
        log.info("编辑合伙人信息：{}", partnerUpdateDTO);

        // 校验合伙人是否存在
        Partner partner = partnerMapper.getById(partnerUpdateDTO.getPartnerId());
        if (partner == null) {
            throw new BaseException(MessageConstant.PARTNER_NOT_FOUND);
        }

        // 更新合伙人信息
        Partner updatePartner = new Partner();
        updatePartner.setId(partnerUpdateDTO.getPartnerId());
        updatePartner.setLevel(partnerUpdateDTO.getLevel());
        updatePartner.setInvestRatio(partnerUpdateDTO.getInvestRatio());

        partnerMapper.update(updatePartner);

        // 注：已参与投票的合伙人修改后，未结束投票任务的权重实时更新
        // 这个逻辑应该在投票计算时动态读取最新的合伙人信息来实现
    }

    /**
     * 禁用合伙人
     * @param partnerId 合伙人ID
     */
    @Override
    public void disable(Long partnerId) {
        log.info("禁用合伙人：{}", partnerId);

        // 校验合伙人是否存在
        Partner partner = partnerMapper.getById(partnerId);
        if (partner == null) {
            throw new BaseException(MessageConstant.PARTNER_NOT_FOUND);
        }

        // 禁用合伙人
        Partner updatePartner = new Partner();
        updatePartner.setId(partnerId);
        updatePartner.setStatus(StatusConstant.DISABLE);

        partnerMapper.update(updatePartner);
    }

    /**
     * 删除合伙人（仅未参与任何投票）
     * @param partnerId 合伙人ID
     */
    @Override
    public void delete(Long partnerId) {
        log.info("删除合伙人：{}", partnerId);

        // 校验合伙人是否存在
        Partner partner = partnerMapper.getById(partnerId);
        if (partner == null) {
            throw new BaseException(MessageConstant.PARTNER_NOT_FOUND);
        }

        // 校验是否参与过投票
        int voteCount = partnerMapper.countVoteRecordsByPartnerId(partnerId);
        if (voteCount > 0) {
            throw new BaseException(MessageConstant.PARTNER_HAS_VOTE_RECORDS);
        }

        // 删除合伙人
        partnerMapper.deleteById(partnerId);
    }
}
