package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void overtimePayment() {
        LocalDateTime now = LocalDateTime.now();
        List<Orders> orders = orderMapper.getOrders(Orders.PENDING_PAYMENT, now.minusMinutes(15));
            for(Orders or : orders){
                or.setStatus(Orders.CANCELLED);
                or.setRejectionReason("未支付");
                or.setCancelTime(LocalDateTime.now());
                orderMapper.update(or);
            }
    }

    @Scheduled(cron = "0 0 1 * * ? ")   // 每天凌晨1点执行
    public void overtimeDelivery(){
        List<Orders> orders = orderMapper.getOrders(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        for(Orders or : orders){
            or.setStatus(Orders.COMPLETED);
            orderMapper.update(or);
        }

    }

}
