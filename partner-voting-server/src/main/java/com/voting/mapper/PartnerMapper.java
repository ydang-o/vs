package com.voting.mapper;

import com.github.pagehelper.Page;
import com.voting.dto.PartnerPageQueryDTO;
import com.voting.entity.Partner;
import com.voting.vo.PartnerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PartnerMapper {

    /**
     * 统计与指定用户绑定的合伙人数
     * @param userId 用户ID
     * @return 关联数量
     */
    @Select("select count(1) from partner where user_id = #{userId}")
    int countByUserId(Long userId);

    /**
     * 新增合伙人
     * @param partner 合伙人信息
     */
    void insert(Partner partner);

    /**
     * 分页查询合伙人
     * @param partnerPageQueryDTO 查询条件
     * @return 合伙人列表
     */
    Page<PartnerVO> pageQuery(PartnerPageQueryDTO partnerPageQueryDTO);

    /**
     * 根据ID查询合伙人
     * @param id 合伙人ID
     * @return 合伙人信息
     */
    Partner getById(Long id);

    /**
     * 更新合伙人信息
     * @param partner 合伙人信息
     */
    void update(Partner partner);

    /**
     * 根据ID删除合伙人
     * @param id 合伙人ID
     */
    void deleteById(Long id);

    /**
     * 统计合伙人参与投票的次数
     * @param partnerId 合伙人ID
     * @return 参与投票次数
     */
    @Select("select count(1) from vote_record where partner_id = #{partnerId}")
    int countVoteRecordsByPartnerId(Long partnerId);
}
