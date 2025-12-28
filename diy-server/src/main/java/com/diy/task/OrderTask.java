package com.diy.task;

import com.diy.entity.Orders;
import com.diy.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理支付超时的订单
     */
    @Scheduled(cron = "0 * * * * ? ")//每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        LocalDateTime time=LocalDateTime.now().plusMinutes(-15);
        //当前时间-下单时间>15
        List<Orders> ordersList=orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT,time);
        for (Orders order : ordersList) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("订单超时,自动取消");
            order.setCancelTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点处理前一天处于派送中的订单
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单:{}",LocalDateTime.now());
        LocalDateTime time=LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList=orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS,time);
        for (Orders order : ordersList) {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        }
    }
}
