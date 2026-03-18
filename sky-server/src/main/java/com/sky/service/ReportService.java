package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param beginDate
     * @param endDate
     * @return
     */
    UserReportVO getUserStatistics(LocalDate beginDate, LocalDate endDate);

    /**
     * 订单统计 指定时间内的订单数据
     * @param beginDate
     * @param endDate
     * @return
     */
    OrderReportVO getOrderStatistics(LocalDate beginDate, LocalDate endDate);

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
