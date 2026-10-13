package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public TurnoverReportVO turnoverReport( LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<Map<String, Object>> turnoverData = orderMapper.getTurnover(beginTime, endTime);
        Map<String, BigDecimal> turnoverMap = new HashMap<>();
        for (Map<String, Object> data : turnoverData){
            Date date = (Date) data.get("date"); // Changed from Date to java.util.Date
            BigDecimal turnover = (BigDecimal) data.get("turnover"); // Changed from Date to Double
            turnoverMap.put(date.toString(), turnover);
        }

        List<String> datalist = new ArrayList<>();
        List<BigDecimal> turnoverList = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)) {
            String date = currentDate.toString();
            datalist.add(date);
            BigDecimal turnover = turnoverMap.getOrDefault(date, BigDecimal.ZERO);
            turnoverList.add(turnover);
            currentDate = currentDate.plusDays(1);
        }

        return new TurnoverReportVO(StringUtils.join(datalist, ","), StringUtils.join(turnoverList, ","));
    }

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {
        List<String> datalist = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)) {
            LocalDateTime beginTime = LocalDateTime.of(currentDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(currentDate, LocalTime.MAX);
            String date = currentDate.toString();
            datalist.add(date);
            newUserList.add(userMapper.getNewUser(beginTime, endTime));
            totalUserList.add(userMapper.getTotalUser(endTime));
            currentDate = currentDate.plusDays(1);
        }

        return new UserReportVO(StringUtils.join(datalist, ","), StringUtils.join(totalUserList, ","), StringUtils.join(newUserList, ","));
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<Map<String, Object>> top10Data = orderMapper.getTop10Orders(beginTime, endTime);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for (Map<String, Object> data : top10Data){
            String name = (String) data.get("dishName");
            Number number1 = (Number) data.get("dishCount");
            Integer number = number1.intValue();
            nameList.add(name);
            numberList.add(number);
        }

        return new SalesTop10ReportVO(StringUtils.join(nameList, ","), StringUtils.join(numberList, ","));

    }

    @Override
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Integer totalOrderCount = orderMapper.getTotalOrderCount(beginTime, endTime);
        Integer effectiveOrderCount = orderMapper.getEffectiveOrderCount(beginTime, endTime);
        Number orderCompletionRate = 0;
        if (totalOrderCount != 0) {
             orderCompletionRate = (effectiveOrderCount * 1.0 / totalOrderCount);
        }
        List<Map<String, Object>> orderCount = orderMapper.getOrderCount(beginTime, endTime);
        List<Map<String, Object>> effectiveCount = orderMapper.getEffectiveCount(beginTime, endTime);

        Map<String, Long> OrderCountMap = new HashMap<>();
        Map<String, Long> effectiveCountMap = new HashMap<>();
        for (Map<String, Object> data : orderCount){
            Date date = (Date) data.get("date"); // Changed from Date to java.util.Date
            Long ordercount = (Long) data.get("OrderCount"); // Changed from Date to Double
            OrderCountMap.put(date.toString(), ordercount);
        }
        for (Map<String, Object> data : effectiveCount){
            Date date = (Date) data.get("date"); // Changed from Date to java.util.Date
            Long effectiveCount1 = (Long) data.get("EffectiveCount"); // Changed from Date to Double
            effectiveCountMap.put(date.toString(), effectiveCount1);
        }

        List<String> datalist = new ArrayList<>();
        List<Long> orderCountList = new ArrayList<>();
        List<Long> effectiveCountList = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)) {
            String date = currentDate.toString();
            datalist.add(date);
            Long effectiveCount1 = effectiveCountMap.getOrDefault(date, 0L);
            effectiveCountList.add(effectiveCount1);

            Long orderCount1 = OrderCountMap.getOrDefault(date, 0L);
            orderCountList.add(orderCount1);
            currentDate = currentDate.plusDays(1);
        }
        return new OrderReportVO(StringUtils.join(datalist, ","), StringUtils.join(orderCountList, ","), StringUtils.join(effectiveCountList, ","), totalOrderCount, effectiveOrderCount, orderCompletionRate.doubleValue());
    }
}
