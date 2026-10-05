package com.sky.controller.admin;


import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")

public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/page")
    public Result getPage(DishPageQueryDTO dishPageQueryDTO) {

        PageResult<DishVO> pageResult = dishService.getPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result save(@RequestBody Dish dish) {
        dishService.insertDish(dish);
        redisTemplate.delete("dish_" +dish.getCategoryId());
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result getDish(@PathVariable Long id) {
        Dish dish = dishService.getDishById(id);
        return Result.success(dish);
    }

    @PutMapping
    public Result update(@RequestBody Dish dish) {
        dishService.updateDish(dish);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    @DeleteMapping

    public Result delete(@RequestParam List<Long> ids) {
        dishService.delete(ids);
        // 删除全部缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status,Long id) {
        dishService.updateStatus(status,id);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(Long categoryId) {
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }



}
