package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询概览数据
        //获取距离今天三十天的日期
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = dateBegin.plusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

        //2.通过POIExcel导出数据
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //创建工作簿基于模板文件
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            //填充数据
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(4).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(5).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

                sheet.getRow(i+7).getCell(1).setCellValue(date.toString());
                sheet.getRow(i+7).getCell(2).setCellValue(businessData.getTurnover());
                sheet.getRow(i+7).getCell(3).setCellValue(businessData.getValidOrderCount());
                sheet.getRow(i+7).getCell(4).setCellValue(businessData.getOrderCompletionRate());
                sheet.getRow(i+7).getCell(5).setCellValue(businessData.getUnitPrice());
                sheet.getRow(i+7).getCell(6).setCellValue(businessData.getNewUsers());
            }


            //3.通过输出流进行文件下载
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //关闭流
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
