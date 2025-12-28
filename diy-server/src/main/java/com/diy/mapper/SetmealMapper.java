package com.diy.mapper;

import com.github.pagehelper.Page;
import com.diy.annotation.AutoFill;
import com.diy.dto.SetmealPageQueryDTO;
import com.diy.entity.Setmeal;
import com.diy.enumeration.OperationType;
import com.diy.vo.DishItemVO;
import com.diy.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 添加套餐
     * @param setmeal
     */
    @AutoFill(value = OperationType.INSERT)
    void add(Setmeal setmeal);

    Page<SetmealVO> getMealList(SetmealPageQueryDTO setmealPageQueryDTO);

    Integer getStatusById(Long id);

    void deleteById(List<Long> ids);
@AutoFill(value = OperationType.UPDATE)
    void updateStatus(Integer status, Long id);

    SetmealVO getMealById(Integer id);

    void update(SetmealVO setmealVO);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);
    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
