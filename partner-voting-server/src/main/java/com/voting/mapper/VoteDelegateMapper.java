package com.voting.mapper;

import com.voting.dto.DelegatePageQueryDTO;
import com.voting.entity.VoteDelegate;
import com.voting.vo.DelegateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteDelegateMapper {

    /**
     * 插入委托记录
     */
    void insert(VoteDelegate voteDelegate);

    /**
     * 分页查询委托列表
     */
    List<DelegateVO> pageQuery(DelegatePageQueryDTO pageQueryDTO);

    /**
     * 根据投票任务ID和委托人ID查询委托记录
     */
    VoteDelegate getByVoteTaskIdAndFromPartnerId(@Param("voteTaskId") Long voteTaskId, @Param("fromPartnerId") Long fromPartnerId);

    /**
     * 根据ID查询委托记录
     */
    VoteDelegate getById(Long id);

    /**
     * 更新委托状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
