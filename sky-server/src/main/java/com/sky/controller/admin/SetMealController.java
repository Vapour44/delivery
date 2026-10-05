package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @GetMapping("/page")
    public Result page(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageResult pg = setMealService.getPage(setmealPageQueryDTO);

        return Result.success(pg);
    }

    @PostMapping
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result insert(@RequestBody SetmealDTO setmealDTO) {
        setMealService.insertSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        SetmealVO setmealVO = setMealService.getById(id);
        return Result.success(setmealVO);
    }

    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        setMealService.deleteBatch(ids);
        return Result.success();
    }

    @PutMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        setMealService.update(setmealDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id) {
        setMealService.startOrStop(status, id);
        return Result.success();
    }

}
