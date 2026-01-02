package com.voting.mapper;

import com.voting.entity.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteRecordMapper {

    /**
     * 插入投票记录
     */
    void insert(VoteRecord voteRecord);

    /**
     * 根据投票任务ID和合伙人ID查询投票记录
     */
    VoteRecord getByVoteTaskIdAndPartnerId(@Param("voteTaskId") Long voteTaskId, @Param("partnerId") Long partnerId);

    /**
     * 根据投票任务ID查询投票记录列表
     */
    List<VoteRecord> listByVoteTaskId(Long voteTaskId);

    /**
     * 统计各选项投票数（人数票）
     */
    Integer countByVoteTaskIdAndOption(@Param("voteTaskId") Long voteTaskId, @Param("voteOption") Integer voteOption);

    /**
     * 查询投票明细（实名模式）
     */
    List<VoteRecord> listDetailByVoteTaskId(Long voteTaskId);
}
