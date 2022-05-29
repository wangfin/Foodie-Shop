package com.imooc.service.center;

import com.imooc.pojo.Orders;
import com.imooc.pojo.vo.center.OrderStatusCountsVO;
import com.imooc.utils.PagedGridResult;

public interface MyOrdersService {

    /**
     * 查询用户自己的订单列表
     * @param userId 用户ID
     * @param orderStatus 订单状态
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryMyOrders(String userId, Integer orderStatus,
                                         Integer page, Integer pageSize);

    /**
     * 将订单状态改为商家发货中
     * @param orderId 订单ID
     */
    public void updateDeliverOrderStatus(String orderId);

    /**
     * 查询我的订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return
     */
    public Orders queryMyOrder(String userId, String orderId);

    /**
     * 将订单状态更改为确认收货
     * @param orderId 订单ID
     * @return
     */
    public boolean updateReceiveOrderStatus(String orderId);

    /**
     * 删除订单（逻辑删除，即不删除数据，而是改变订单的状态）
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return
     */
    public boolean deleteOrder(String userId, String orderId);

    /**
     * 查询用户不同状态订单数
     * @param userId 用户ID
     */
    public OrderStatusCountsVO getOrderStatusCounts(String userId);

    /**
     * 查询用户订单动向
     * @param userId 用户ID
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getOrdersTrend(String userId,
                                         Integer page, Integer pageSize);
}
