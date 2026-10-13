package com.sky.service.impl;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.WebSocket.WebSocketServer;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
        @Autowired
        private ShoppingCartMapper shoppingCartMapper;
        @Autowired
        private AddressBookMapper addressBookMapper;
        @Autowired
        private OrderMapper orderMapper;
        @Autowired
        private OrderDetailMapper orderDetailMapper;
        @Autowired
        private WebSocketServer webSocketServer;


        @Transactional(rollbackFor = Exception.class)
        @Override
        public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

                //处理异常情况(地址为空，购物车为空)
                if (ordersSubmitDTO.getAddressBookId() == null) {
                        throw new AddressBookBusinessException("地址为空");
                }
                Long userId = BaseContext.getCurrentId();
                ShoppingCart shoppingCart = new ShoppingCart();
                shoppingCart.setUserId(userId);
                List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
                if (shoppingCartList == null || shoppingCartList.isEmpty()) {
                        throw new ShoppingCartBusinessException("购物车为空");
                }
                //将数据插入订单表
                Orders order = new Orders();
                BeanUtils.copyProperties(ordersSubmitDTO, order);
                order.setUserId(userId);
                order.setNumber(String.valueOf(System.currentTimeMillis()));
                order.setStatus(Orders.PENDING_PAYMENT);
                order.setOrderTime(LocalDateTime.now());
                order.setPayStatus(Orders.UN_PAID);
                AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
                String address = addressBook.getProvinceName()
                        + addressBook.getCityName()
                        + addressBook.getDistrictName()
                        + addressBook.getDetail();
                order.setAddress(address);
                order.setPhone(addressBook.getPhone());
                order.setConsignee(addressBook.getConsignee());
                orderMapper.insert(order);

                //将数据插入订单明细表
                for (ShoppingCart Cart : shoppingCartList) {
                        OrderDetail orderDetail = new OrderDetail();
                        BeanUtils.copyProperties(Cart, orderDetail);
                        orderDetail.setOrderId(order.getId());

                        orderDetailMapper.insert(orderDetail);
                }

                //清空购物车
                shoppingCartMapper.delete(shoppingCart);

                return OrderSubmitVO.builder()
                        .id(order.getId())
                        .orderAmount(order.getAmount())
                        .orderTime(order.getOrderTime())
                        .orderNumber(order.getNumber())
                        .build();
        }

        @Override
        public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
                PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
                ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
                Page<Orders> page = orderMapper.historyOrders(ordersPageQueryDTO);
                List<OrderVO> orderVOList = new ArrayList<>();
                if(page != null&&!page.isEmpty()){
                        for(Orders orders : page){
                                Long orderId = orders.getId();
                                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                                OrderVO orderVO = new OrderVO();
                                BeanUtils.copyProperties(orders, orderVO);
                                orderVO.setOrderDetailList(orderDetails);

                                orderVOList.add(orderVO);
                        }
                }
                return new PageResult<>(page.getTotal(), orderVOList);
        }

        @Override
        public OrderVO orderDetail(Long id) {
                Orders orders = orderMapper.getById(id);
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                return orderVO;

        }

        @Override
        public void userCancelById(Long id) {
                Orders ordersDB = orderMapper.getById(id);

                // 校验订单是否存在
                if (ordersDB == null) {
                        throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
                }

                //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
                if (ordersDB.getStatus() > 2) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }

                Orders orders = new Orders();
                orders.setId(ordersDB.getId());

                // 订单处于待接单状态下取消，需要进行退款
                /*if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                        //调用微信支付退款接口
                        weChatPayUtil.refund(
                                ordersDB.getNumber(), //商户订单号
                                ordersDB.getNumber(), //商户退款单号
                                new BigDecimal(0.01),//退款金额，单位 元
                                new BigDecimal(0.01));//原订单金额

                        //支付状态修改为 退款
                        orders.setPayStatus(Orders.REFUND);
                }*/

                // 更新订单状态、取消原因、取消时间
                orders.setStatus(Orders.CANCELLED);
                orders.setPayStatus(Orders.REFUND);
                orders.setCancelReason("用户取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
        }

        @Override
        public void repetitionOrder(Long id) {
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
                for (OrderDetail orderDetail : orderDetailList){
                        ShoppingCart shoppingCart = new ShoppingCart();
                        BeanUtils.copyProperties(orderDetail, shoppingCart);
                        shoppingCart.setUserId(BaseContext.getCurrentId());
                        shoppingCart.setCreateTime(LocalDateTime.now());
                        shoppingCartMapper.add(shoppingCart);
                }
        }

        @Override
        public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
                PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
                ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
                Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);
                List<OrderVO> orderVOList = new ArrayList<>();
                if(page != null&&!page.isEmpty()){
                        for(Orders orders : page){
                                Long orderId = orders.getId();
                                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                                OrderVO orderVO = new OrderVO();
                                BeanUtils.copyProperties(orders, orderVO);
                                orderVO.setOrderDetailList(orderDetails);
                                StringBuilder orderDishes = new StringBuilder();

                                for (OrderDetail orderDetail : orderDetails) {
                                        orderDishes.append(orderDetail.getName())
                                                .append(" x")
                                                .append(orderDetail.getNumber())
                                                .append(",")
                                                .append("\n");
                                }

                                orderVO.setOrderDishes(orderDishes.toString());

                                orderVOList.add(orderVO);
                        }
                }
                return new PageResult<>(page.getTotal(), orderVOList);

        }

        @Override
        public OrderStatisticsVO statistics() {
                // 根据状态，分别查询出待接单、待派送、派送中的订单数量
                Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
                Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
                Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

                // 将查询出的数据封装到orderStatisticsVO中响应
                OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
                orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
                orderStatisticsVO.setConfirmed(confirmed);
                orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
                return orderStatisticsVO;
        }

        @Override
        public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
                Orders orders = Orders.builder()
                        .id(ordersConfirmDTO.getId())
                        .status(Orders.CONFIRMED)
                        .build();

                orderMapper.update(orders);
        }

        @Override
        public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
                // 根据id查询订单
                Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

                // 订单只有存在且状态为2（待接单）才可以拒单
                if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }

                //支付状态
                /*Integer payStatus = ordersDB.getPayStatus();
                if (payStatus == Orders.PAID) {
                        //用户已支付，需要退款
                        String refund = weChatPayUtil.refund(
                                ordersDB.getNumber(),
                                ordersDB.getNumber(),
                                new BigDecimal(0.01),
                                new BigDecimal(0.01));

                }*/

                // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
                Orders orders = new Orders();
                orders.setId(ordersDB.getId());
                orders.setStatus(Orders.CANCELLED);
                orders.setPayStatus(Orders.REFUND);
                orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
                orders.setCancelTime(LocalDateTime.now());

                orderMapper.update(orders);
        }

        @Override
        public void delivery(Long id) {
                // 根据id查询订单
                Orders ordersDB = orderMapper.getById(id);

                // 校验订单是否存在，并且状态为3
                if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }

                Orders orders = new Orders();
                orders.setId(ordersDB.getId());
                // 更新订单状态,状态转为派送中
                orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

                orderMapper.update(orders);
        }

        @Override
        public void complete(Long id) {
                // 根据id查询订单
                Orders ordersDB = orderMapper.getById(id);

                // 校验订单是否存在，并且状态为4
                if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }

                Orders orders = new Orders();
                orders.setId(ordersDB.getId());
                // 更新订单状态,状态转为完成
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());

                orderMapper.update(orders);
        }

        @Override
        public void payment(OrdersPaymentDTO ordersPaymentDTO) {

                Orders query = new Orders();
                Long userId = BaseContext.getCurrentId();
                query.setNumber(ordersPaymentDTO.getOrderNumber());
                query.setUserId(userId);

                Orders order = orderMapper.getByNumber(query);
                if (order == null) {
                        throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
                }
                if (!Orders.PENDING_PAYMENT.equals(order.getStatus())
                        || !Orders.UN_PAID.equals(order.getPayStatus())) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }

                Orders update = Orders.builder()
                        .id(order.getId())
                        .status(Orders.TO_BE_CONFIRMED)
                        .payStatus(Orders.PAID)
                        .checkoutTime(LocalDateTime.now())
                        .build();
                orderMapper.update(update);
                Map map = new HashMap();
                map.put("type", 1);
                map.put("orderId", order.getId());
                map.put("content",order.getNumber());
                String json = JSON.toJSONString(map);
                webSocketServer.sendToAllClient(json);
        }

        @Override
        public void cancel(OrdersCancelDTO ordersCancelDTO) {
                // 根据id查询订单
                Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

                /*//支付状态
                Integer payStatus = ordersDB.getPayStatus();
                if (payStatus == 1) {
                        //用户已支付，需要退款
                        String refund = weChatPayUtil.refund(
                                ordersDB.getNumber(),
                                ordersDB.getNumber(),
                                new BigDecimal(0.01),
                                new BigDecimal(0.01));
                        log.info("申请退款：{}", refund);
                }*/

                // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
                Orders orders = new Orders();
                orders.setId(ordersCancelDTO.getId());
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(ordersCancelDTO.getCancelReason());
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
        }

        @Override
        public void reminder(Long id) {
                Orders orders = orderMapper.getById(id);
                if (orders == null) {
                        throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
                }
                Map map = new HashMap();
                map.put("type", 2);
                map.put("orderId", orders.getId());
                map.put("content",orders.getNumber());
                String json = JSON.toJSONString(map);
                webSocketServer.sendToAllClient(json);
        }


}
