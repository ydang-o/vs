package com.diy.mapper;

import com.diy.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    //select * from mealdish where id in()
    List<Long> getSetmealDishIds(List<Long> Ids);

    void SetDish(List<SetmealDish> mealDishes);

    void deleteDishById(List<Long> ids);


    List<SetmealDish> getDishesById(Integer id);

    void deleteOriginDishById(Long id);

    void insert(List<SetmealDish> setmealDishes);
}
