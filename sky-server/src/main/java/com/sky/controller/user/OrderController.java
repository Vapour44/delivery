package com.sky.controller.user;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")

public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    public Result submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {

            return Result.success(orderService.submitOrder(ordersSubmitDTO));
    }
    @GetMapping("/historyOrders")
    public Result historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/orderDetail/{id}")
    public Result orderDetail(@PathVariable Long id) {
        return Result.success(orderService.orderDetail(id));
    }
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        orderService.userCancelById(id);
        return Result.success();
    }
    @PostMapping("/repetition/{id}")
    public Result repetitionOrder(@PathVariable Long id) {
        orderService.repetitionOrder(id);
        return Result.success();
    }
    @PutMapping("/payment")
    public Result payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        orderService.payment(ordersPaymentDTO);
        return Result.success();
    }
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }

}
