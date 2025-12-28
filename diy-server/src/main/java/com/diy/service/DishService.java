package com.diy.service;

import com.diy.dto.DishDTO;
import com.diy.dto.DishPageQueryDTO;
import com.diy.entity.Dish;
import com.diy.result.PageResult;
import com.diy.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);
//根据id修改基本菜品信息和口味信息
    void updateWithFlavor(DishDTO dishDTO);

   List<Dish> getByCategoryId(Integer categoryId);

    List<DishVO> listWithFlavor(Dish dish);
}
