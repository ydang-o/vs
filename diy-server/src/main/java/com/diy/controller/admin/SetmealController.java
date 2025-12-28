package com.diy.controller.admin;

import com.diy.dto.SetmealDTO;
import com.diy.dto.SetmealPageQueryDTO;
import com.diy.result.PageResult;
import com.diy.result.Result;
import com.diy.service.SetmealService;
import com.diy.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐接口
     * @param setmealDTO
     * @return
     */
    @ApiOperation("新增套餐接口")
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result addMeal(@RequestBody SetmealDTO setmealDTO){
        setmealService.add(setmealDTO);
        return Result.success();
    }

    /**
     * 分页查询
     * @param
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> mealPage(SetmealPageQueryDTO setmealPageQueryDTO){
       PageResult pageResult= setmealService.pageList(setmealPageQueryDTO);
        System.out.println("获取的数据:"+setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     *批量删除套餐
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 启用禁用套餐
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result Status(@PathVariable Integer status,Long id){
        setmealService.StartOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据id查询套餐(用于修改时信息回显)
     * @return
     */
    @GetMapping("/{id}")
    public Result getMealById(@PathVariable Integer id){
       SetmealVO setmealVO= setmealService.getMealById(id);
       return Result.success(setmealVO);
    }

    /**
     * 更新套餐
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result updateMeal(@RequestBody SetmealVO setmealVO){
        setmealService.update(setmealVO);
        return Result.success();
    }
}
