package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    List<DishVO> getPage(DishPageQueryDTO dishPageQueryDTO);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    void insertDish(Dish dish);

    Dish getDishById(Long id);


    @AutoFill(OperationType.UPDATE)
    void updateDish(Dish dish);

    void delete(List<Long> ids);

    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> list(Dish dish);

    int countDishInSet(Dish dish);
}
