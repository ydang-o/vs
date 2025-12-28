package com.diy.mapper;

import com.github.pagehelper.Page;
import com.diy.dto.GoodsSalesDTO;
import com.diy.dto.OrdersPageQueryDTO;
import com.diy.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    Orders getDetailsById(Long id);
@Select("select count(id) from orders where status=#{status}")
    Integer countStatus(Integer status);
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);
@Update("update orders set status=#{status} where id=#{id}")
    void updateStatus(Orders order);
@Select("select * from orders where status=#{pendingPayment} and order_time<#{time}")
    List<Orders> getByStatusAndOrdertimeLT(Integer pendingPayment, LocalDateTime time);

    /**
     * 依据动态条件统计
     * @param map
     * @return
     */
    Double sumByMap(java.util.Map map);

    Integer countByMap(Map map);

    /**
     * 统计指定区间内的销量前十的菜品
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop(LocalDateTime begin,LocalDateTime end);
}
