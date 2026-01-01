package com.voting.mapper;

import com.github.pagehelper.Page;
import com.voting.dto.PartnerPageQueryDTO;
import com.voting.entity.Partner;
import com.voting.vo.PartnerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 根据用户ID查询合伙人
     * @param userId 用户ID
     * @return 合伙人信息
     */
    @Select("select * from partner where user_id = #{userId}")
    Partner getByUserId(Long userId);

    /**
     * 根据筛选条件查询合伙人列表
     * @param levels 层级列表
     * @param status 状态
     * @return 合伙人列表
     */
    List<Partner> listByFilter(@Param("levels") List<Integer> levels, @Param("status") Integer status);
}
