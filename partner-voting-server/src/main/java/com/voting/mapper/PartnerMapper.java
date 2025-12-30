package com.voting.mapper;

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
}
