package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
     OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     *
     * 订单催单
     * @param id
     */
    void reminder(Long id);

    /**
     * 用户端订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQuery4User(Integer page, Integer pageSize, Integer status);

    /**
     * 查询订单详情
     * @param orderId
     * @return
     */
     OrderVO details(Long orderId);

     /**
     * 用户取消订单
     * @param orderId
     */
    void cancel(Long orderId) throws Exception;

    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 用户再来一单
     * @param orderId
     */
    void repetition(Long orderId);



     /**
     * 管理员端订单分页查询
     * @param orderPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO orderPageQueryDTO);


    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();


    /**
     * 订单确认
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 订单拒绝
     * @param ordersRejectionDTO
     */
    void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 订单派送
     * @param id
     */
    void delivery(Long id);

    /**
     * 订单完成
     * @param id
     */
    void complete(Long id);
}
