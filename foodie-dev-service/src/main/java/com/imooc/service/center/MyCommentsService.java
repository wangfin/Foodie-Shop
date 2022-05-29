package com.imooc.service.center;

import com.imooc.pojo.OrderItems;
import com.imooc.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

public interface MyCommentsService {

    /**
     * 根据订单ID查询关联的商品
     * @param orderId 订单ID
     * @return
     */
    public List<OrderItems> queryPendingComment(String orderId);

    /**
     * 保存商品评价信息
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param commentList 评价内容
     */
    public void saveComments(String userId, String orderId, List<OrderItemsCommentBO> commentList);

    /**
     * 查询我的历史评价
     * @param userId 用户ID
     * @param page 分页参数
     * @param pageSize 分页参数
     * @return
     */
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);
}
