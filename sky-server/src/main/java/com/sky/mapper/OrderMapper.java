package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders order);

    Page<Orders> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders orders);

    Page<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{deliveryInProgress}")
    Integer countStatus(Integer deliveryInProgress);

    @Select("select * from orders where number = #{number} and user_id = #{userId}")
    Orders getByNumber(Orders orders);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getOrders(Integer status, LocalDateTime orderTime);

    List<Map<String, Object>> getTurnover(LocalDateTime begin, LocalDateTime end);

    List<Map<String, Object>> getTop10Orders(LocalDateTime begin, LocalDateTime end);

    List<Map<String, Object>> getOrderCount(LocalDateTime beginTime, LocalDateTime endTime);
    @Select("select count(id) from orders where order_time >= #{beginTime} and order_time <= #{endTime} ")
    Integer getTotalOrderCount(LocalDateTime beginTime, LocalDateTime endTime);
    @Select("select count(id) from orders where status = 5 and order_time >= #{beginTime} and order_time <= #{endTime} ")
    Integer getEffectiveOrderCount(LocalDateTime beginTime, LocalDateTime endTime);

    List<Map<String, Object>> getEffectiveCount(LocalDateTime beginTime, LocalDateTime endTime);
}
