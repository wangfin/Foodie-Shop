package com.imooc.service;

import com.imooc.pojo.Carousel;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.OrderVO;

import java.util.List;

public interface OrderService {

    /**
     * 用户创建订单
     * @param submitOrderBO 订单BO
     * @return
     */
    public OrderVO createOrder(SubmitOrderBO submitOrderBO);

    /**
     * 修改订单状态
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     */
    public void updateOrderStatus(String orderId, Integer orderStatus);

    /**
     * 查询订单状态
     * @param orderId 订单ID
     * @return
     */
    public OrderStatus queryOrderStatusInfo(String orderId);

    /**
     * 每隔固定时间，关闭还没有完成付款的订单
     */
    public void closeOrder();
}
