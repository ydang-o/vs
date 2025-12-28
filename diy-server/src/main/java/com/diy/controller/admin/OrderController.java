package com.diy.controller.admin;

import com.diy.dto.OrdersCancelDTO;
import com.diy.dto.OrdersConfirmDTO;
import com.diy.dto.OrdersPageQueryDTO;
import com.diy.dto.OrdersRejectionDTO;
import com.diy.entity.Orders;
import com.diy.result.PageResult;
import com.diy.result.Result;
import com.diy.service.OrderService;
import com.diy.vo.OrderStatisticsVO;
import com.diy.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "用户订单相关")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("条件查询订单")
    public Result<PageResult> search(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 统计各个状态的订单数量
     * @return
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> countStatus(){
        OrderStatisticsVO orderStatisticsVO=orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id){
        OrderVO details = orderService.getDetails(id);
        return Result.success(details);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒绝接单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
   public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
   }

    /**
     * 派送订单
     * @param id
     * @return
     */
   @PutMapping("/delivery/{id}")
   public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
   }

    /**
     * 完成订单
     * @param id
     * @return
     */
   @PutMapping("/complete/{id}")
   public Result complete(@PathVariable Long id){
       orderService.complete(id);
       return Result.success();
   }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
   @PutMapping("/cancel")
   public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
       orderService.cancel(ordersCancelDTO);
       return Result.success();
   }

}
