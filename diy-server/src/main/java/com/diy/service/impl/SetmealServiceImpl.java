package com.diy.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.diy.constant.MessageConstant;
import com.diy.constant.StatusConstant;
import com.diy.dto.SetmealDTO;
import com.diy.dto.SetmealPageQueryDTO;
import com.diy.entity.Setmeal;
import com.diy.entity.SetmealDish;
import com.diy.exception.DeletionNotAllowedException;
import com.diy.mapper.CategoryMapper;
import com.diy.mapper.SetmealDishMapper;
import com.diy.mapper.SetmealMapper;
import com.diy.result.PageResult;
import com.diy.service.SetmealService;
import com.diy.vo.DishItemVO;
import com.diy.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void add(SetmealDTO setmealDTO) {
        //设置套餐表,并主键回显
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.add(setmeal);
        //添加选择的菜品进套餐,并关联起来
        List<SetmealDish> mealDishes = new ArrayList<>();
        //获得套餐里面的菜品
        mealDishes = setmealDTO.getSetmealDishes();
        //设置菜品所属的套餐Id
        for (SetmealDish mealDish : mealDishes) {
            mealDish.setSetmealId(setmeal.getId());
        }
        //设置套餐关联的菜品表
        setmealDishMapper.SetDish(mealDishes);
        //设置套餐表
    }

    @Override
    public PageResult pageList(SetmealPageQueryDTO setmealPageQueryDTO) {
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);
        Page<SetmealVO> mealList = setmealMapper.getMealList(setmealPageQueryDTO);
        //查询对应的套餐信息
        for (SetmealVO setmealVO : mealList) {
            setmealVO.setCategoryName(categoryMapper.getCategoryById(setmealVO.getCategoryId()));
        }
        return new PageResult(mealList.getTotal(), mealList.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        //查看套餐状态
        for (Long id : ids) {
            //处于起售中的不能被删除
            if (setmealMapper.getStatusById(id) == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐
        setmealMapper.deleteById(ids);
        //删除套餐包含的菜品
        setmealDishMapper.deleteDishById(ids);
    }

    /**
     * 启用禁用套餐
     * @param status
     * @param id
     */

    @Override
    public void StartOrStop(Integer status, Long id) {
        //更新套餐状态
        setmealMapper.updateStatus(status,id);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getMealById(Integer id) {
        //先查基本的套餐信息
       SetmealVO setmealVO=setmealMapper.getMealById(id);
       //再查套餐包含的菜品
        List<SetmealDish> setmealDishes=setmealDishMapper.getDishesById(id);
        setmealVO.setSetmealDishes(setmealDishes);
       return setmealVO;
    }

    /**
     * 更新套餐
     * @param setmealVO
     */
    @Override
    public void update(SetmealVO setmealVO) {
        //更新套餐
        setmealMapper.update(setmealVO);
        //更新套餐对应的菜品表
        //1.先删除原来的菜品,再重新添加
        List<SetmealDish> setmealDishes=setmealVO.getSetmealDishes();
        System.out.println(setmealDishes);
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealVO.getId());
        }
        System.out.println("更新后:"+setmealDishes);
        setmealDishMapper.deleteOriginDishById(setmealVO.getId());
        //2.更新套餐菜品
       setmealDishMapper.insert(setmealDishes);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
