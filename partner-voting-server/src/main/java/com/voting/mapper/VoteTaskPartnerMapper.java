package com.voting.mapper;

import com.voting.entity.VoteTaskPartner;
import com.voting.vo.MyVoteTaskVO;
import com.voting.vo.VoteTaskPartnerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteTaskPartnerMapper {

    /**
     * 批量插入投票任务参与人
     */
    void batchInsert(List<VoteTaskPartner> partnerList);

    /**
     * 根据投票任务ID查询参与人列表
     */
    List<VoteTaskPartnerVO> listByVoteTaskId(Long voteTaskId);

    /**
     * 根据投票任务ID和合伙人ID查询参与人
     */
    VoteTaskPartner getByVoteTaskIdAndPartnerId(@Param("voteTaskId") Long voteTaskId, @Param("partnerId") Long partnerId);

    /**
     * 更新投票状态
     */
    void updateHasVoted(@Param("voteTaskId") Long voteTaskId, @Param("partnerId") Long partnerId);

    /**
     * 统计参与人数
     */
    Integer countByVoteTaskId(Long voteTaskId);

    /**
     * 统计已投票人数
     */
    Integer countVotedByVoteTaskId(Long voteTaskId);

    /**
     * 我的待投票列表
     */
    List<MyVoteTaskVO> pageMyPending(@Param("partnerId") Long partnerId);

    /**
     * 我的已投票列表
     */
    List<MyVoteTaskVO> pageMyVoted(@Param("partnerId") Long partnerId);
}
