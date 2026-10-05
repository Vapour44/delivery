package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;



    @Override
    public PageResult<DishVO> getPage(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = (Page<DishVO>)dishMapper.getPage(dishPageQueryDTO);

        return new PageResult<DishVO>(page.getTotal(), page.getResult());
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertDish(Dish dish) {
        dishMapper.insertDish(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dish.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public Dish getDishById(Long id) {
        Dish dish = dishMapper.getDishById(id);
        dish.setFlavors(dishFlavorMapper.selectByDishId(id));

        return dish;

    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDish(Dish dish) {
        dishMapper.updateDish(dish);
        //修改时会先删除再插入
        dishFlavorMapper.deleteByDishId(dish.getId());

        List<DishFlavor> flavors = dish.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<Long> ids) {

        dishFlavorMapper.deleteByDishIds(ids);
        dishMapper.delete(ids);
    }

    @Override
    public void updateStatus(Integer status,Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        int dishCount = dishMapper.countDishInSet(dish);
        if(status==0&&dishCount > 0){
            throw new BaseException("存在套餐关联，不能下架");
        }
        dishMapper.updateDish(dish);
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        return dishMapper.list(dish);

    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
