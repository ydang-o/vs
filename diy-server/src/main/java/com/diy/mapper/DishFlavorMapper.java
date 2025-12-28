package com.diy.mapper;

import com.diy.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import javax.validation.constraints.Digits;
import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入
     * @param flavors
     */
    static void insertBatch(List<DishFlavor> flavors) {
    }
@Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteById(Long dishId);

    void deleteByIds(List<Long> dishIds);
@Select("select * from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
