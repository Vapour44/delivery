package com.sky.service;

import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    PageResult getPage(DishPageQueryDTO dishPageQueryDTO);

    void insertDish(Dish dish);


    Dish getDishById(Long id);

    void updateDish(Dish dish);

    void delete(List<Long> ids);

    void updateStatus(Integer status,Long id);

    List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
