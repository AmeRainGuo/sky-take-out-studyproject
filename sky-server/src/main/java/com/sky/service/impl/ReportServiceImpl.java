package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        // 1. 时间范围
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //计算并添加到 dateList
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //stream流尝试解决
        //String dateListString = dateList.stream().map(localDate->localDate.toString()).collect(Collectors.joining(","));
        String dateListString = StringUtils.join(dateList, ",");
        turnoverReportVO.setDateList(dateListString);

        // 2. 查询订单数据
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//LocalDateTime.of(date, LocalTime.MIN);构建时间方法 第一个参数是日期部分 第二个参数是时间部分
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //使用map集合增加查询条件的可拓展性
            Map map = new HashMap();
            map.put("orderTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);

            if(turnover == null){
                turnover = 0.0;
            }

            turnoverList.add(turnover);
        }

        String turnoverListString = turnoverList.stream().map(turnover->turnover.toString()).collect(Collectors.joining(","));
        turnoverReportVO.setTurnoverList(turnoverListString);
        return null;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //计算并添加到 dateList
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListString = StringUtils.join(dateList, ",");
        userReportVO.setDateList(dateListString);

        // 2. 查询订单数据
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//LocalDateTime.of(date, LocalTime.MIN);构建时间方法 第一个参数是日期部分 第二个参数是时间部分
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //使用map集合增加查询条件的可拓展性
            Map map = new HashMap();
            map.put("begin", beginTime);
            Integer newUserCount = userMapper.countUser(map);

            map.put("end", endTime);
            Integer totalUserCount = userMapper.countUser(map);

            if(newUserCount == null){
                newUserCount = 0;
            }

            newUserList.add(newUserCount);
            totalUserList.add(totalUserCount);
        }

        String newUserListString = newUserList.stream().map(newUser->newUser.toString()).collect(Collectors.joining(","));
        userReportVO.setNewUserList(newUserListString);
        String totalUserListString = totalUserList.stream().map(totalUser->totalUser.toString()).collect(Collectors.joining(","));
        userReportVO.setTotalUserList(totalUserListString);
        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO = new OrderReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //计算并添加到 dateList
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateListString = StringUtils.join(dateList, ",");
        orderReportVO.setDateList(dateListString);

        //遍历dataList集合，获取每个日期对应的订单数据
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//LocalDateTime.of(date, LocalTime.MIN);构建时间方法 第一个参数是日期部分 第二个参数是时间部分
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer orderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        orderReportVO.setTotalOrderCount(totalOrderCount);

        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        orderReportVO.setValidOrderCount(validOrderCount);

        String orderCountListString = orderCountList.stream().map(orderCount->orderCount.toString()).collect(Collectors.joining(","));
        orderReportVO.setOrderCountList(orderCountListString);

        String validOrderCountListString = validOrderCountList.stream().map(validOrderCounts->validOrderCounts.toString()).collect(Collectors.joining(","));
        orderReportVO.setValidOrderCountList(validOrderCountListString);

        //订单完成率
        if(totalOrderCount != 0){
            Double orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            orderReportVO.setOrderCompletionRate(orderCompletionRate);
        }else{
            orderReportVO.setOrderCompletionRate(0.0);
        }

        return orderReportVO;
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        Integer count = orderMapper.countByMap(map);
        if(count == null){
            count = 0;
        }
        return count;
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);//LocalDateTime.of(date, LocalTime.MIN);构建时间方法 第一个参数是日期部分 第二个参数是时间部分
            LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

            List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

            List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
            String nameListString = StringUtils.join(nameList, ",");

            List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
            String numberListString = StringUtils.join(numberList, ",");

            return SalesTop10ReportVO.builder()
                    .nameList(nameListString)
                    .numberList(numberListString)
                    .build();
    }
}
