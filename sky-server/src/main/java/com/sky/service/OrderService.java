package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) ;

    OrderVO orderDetail(Long id);

    void userCancelById(Long id);

    void repetitionOrder(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void delivery(Long id);

    void complete(Long id);

    void payment(OrdersPaymentDTO ordersPaymentDTO);

    void cancel(OrdersCancelDTO ordersCancelDTO);

    void reminder(Long id);
}
