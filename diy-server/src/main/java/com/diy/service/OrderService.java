package com.diy.service;

import com.diy.dto.*;
import com.diy.result.PageResult;
import com.diy.vo.OrderPaymentVO;
import com.diy.vo.OrderStatisticsVO;
import com.diy.vo.OrderSubmitVO;
import com.diy.vo.OrderVO;

public interface OrderService {
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



    PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getDetails(Long id);

    void update(Long id);

    void repetition(Long id);


    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void delivery(Long id);

    void complete(Long id);

    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 用户催单
     * @param id
     */
    void reminder(Long id);
}
