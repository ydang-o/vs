package com.voting.mapper;

import com.voting.dto.VoteTaskPageQueryDTO;
import com.voting.entity.VoteTask;
import com.voting.vo.VoteTaskDetailVO;
import com.voting.vo.VoteTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteTaskMapper {

    /**
     * 插入投票任务
     */
    void insert(VoteTask voteTask);

    /**
     * 根据ID查询投票任务
     */
    VoteTask getById(Long id);

    /**
     * 分页查询投票任务列表
     */
    List<VoteTaskVO> pageQuery(VoteTaskPageQueryDTO pageQueryDTO);

    /**
     * 查询投票任务详情
     */
    VoteTaskDetailVO getDetailById(Long id);

    /**
     * 更新投票任务状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据议案ID查询投票任务
     */
    VoteTask getByProposalId(Long proposalId);
}
