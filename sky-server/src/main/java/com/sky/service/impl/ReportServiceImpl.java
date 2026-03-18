package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
}
