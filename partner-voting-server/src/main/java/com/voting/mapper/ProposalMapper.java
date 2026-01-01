package com.voting.mapper;

import com.github.pagehelper.Page;
import com.voting.dto.ProposalPageQueryDTO;
import com.voting.entity.Proposal;
import com.voting.vo.ProposalDetailVO;
import com.voting.vo.ProposalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 议案Mapper接口
 */
@Mapper
public interface ProposalMapper {

    /**
     * 新增议案
     * @param proposal 议案信息
     */
    void insert(Proposal proposal);

    /**
     * 分页查询议案
     * @param proposalPageQueryDTO 查询条件
     * @return 议案列表
     */
    Page<ProposalVO> pageQuery(ProposalPageQueryDTO proposalPageQueryDTO);

    /**
     * 根据ID查询议案
     * @param id 议案ID
     * @return 议案信息
     */
    Proposal getById(Long id);

    /**
     * 根据ID查询议案详情
     * @param id 议案ID
     * @return 议案详情
     */
    ProposalDetailVO getDetailById(Long id);

    /**
     * 更新议案信息
     * @param proposal 议案信息
     */
    void update(Proposal proposal);

    /**
     * 根据ID删除议案
     * @param id 议案ID
     */
    void deleteById(Long id);

    /**
     * 查询今年的议案数量（用于生成编号）
     * @param year 年份
     * @return 议案数量
     */
    @Select("select count(1) from proposal where YEAR(create_time) = #{year}")
    int countByYear(int year);

    /**
     * 更新议案状态
     * @param id 议案ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 统计议案关联的投票任务数量
     * @param proposalId 议案ID
     * @return 投票任务数量
     */
    @Select("select count(1) from vote_task where proposal_id = #{proposalId}")
    int countVoteTasksByProposalId(Long proposalId);
}
