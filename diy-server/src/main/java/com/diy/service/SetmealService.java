package com.diy.service;

import com.diy.dto.SetmealDTO;
import com.diy.dto.SetmealPageQueryDTO;
import com.diy.entity.Setmeal;
import com.diy.result.PageResult;
import com.diy.vo.DishItemVO;
import com.diy.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    void add(SetmealDTO setmealDTO);

    PageResult pageList(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteByIds(List<Long> ids);

    void StartOrStop(Integer status, Long id);

    SetmealVO getMealById(Integer id);

    void update(SetmealVO setmealVO);
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
