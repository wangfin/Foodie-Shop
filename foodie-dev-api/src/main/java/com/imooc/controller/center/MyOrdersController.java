package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.Orders;
import com.imooc.pojo.vo.center.OrderStatusCountsVO;
import com.imooc.service.center.MyOrdersService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Api(value = "用户中心-我的订单", tags = {"我的订单相关的api接口"})
@RestController
@RequestMapping("myorders")
public class MyOrdersController extends BaseController {

    @Autowired
    private MyOrdersService myOrdersService;

    /**
     * 查询我的订单列表
     *
     * @return
     */
    @ApiOperation(value = "查询我的订单列表", notes = "查询我的订单列表", httpMethod = "POST")
    // 这里的参数不是路径参数了，是请求参数
    @PostMapping("/query")
    public IMOOCJSONResult query(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderStatus", value = "订单状态", required = false)
            @RequestParam Integer orderStatus,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize) {

        // 判断输入是否为空
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        // 判断page是否为空，如果不为空，则设置为默认的1
        if (page == null) {
            page = 1;
        }

        // 设置默认pageSize
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询我的订单，具有分页
        PagedGridResult grid = myOrdersService.queryMyOrders(userId, orderStatus, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }

    // 商家发货没有后端，所以这个接口仅仅只是用于模拟
    @ApiOperation(value = "商家发货", notes = "商家发货", httpMethod = "GET")
    @GetMapping("/deliver")
    public IMOOCJSONResult deliver(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId) throws Exception {

        if (StringUtils.isBlank(orderId)) {
            return IMOOCJSONResult.errorMsg("订单ID不能为空");
        }
        myOrdersService.updateDeliverOrderStatus(orderId);
        return IMOOCJSONResult.ok();
    }

    // 用户确认收货
    @ApiOperation(value = "用户确认收货", notes = "用户确认收货", httpMethod = "POST")
    @PostMapping("/confirmReceive")
    public IMOOCJSONResult confirmReceive(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId) throws Exception {

        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }

        boolean res = myOrdersService.updateReceiveOrderStatus(orderId);
        if (!res) {
            return IMOOCJSONResult.errorMsg("订单确认收货失败！");
        }

        return IMOOCJSONResult.ok();
    }

    // 用户删除订单
    @ApiOperation(value = "用户删除订单", notes = "用户删除订单", httpMethod = "POST")
    @PostMapping("/delete")
    public IMOOCJSONResult delete(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId) throws Exception {

        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }

        System.out.println(userId);
        System.out.println(orderId);
        boolean res = myOrdersService.deleteOrder(userId, orderId);
        if (!res) {
            return IMOOCJSONResult.errorMsg("订单删除失败！");
        }

        return IMOOCJSONResult.ok();
    }


    /**
     * 获取不同状态类别的订单数量
     *
     * @return
     */
    @ApiOperation(value = "获取不同状态类别的订单数量", notes = "获取不同状态类别的订单数量", httpMethod = "POST")
    // 这里的参数不是路径参数了，是请求参数
    @PostMapping("/statusCounts")
    public IMOOCJSONResult statusCounts(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId) {

        // 判断输入是否为空
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        // 查询订单数量
        OrderStatusCountsVO result = myOrdersService.getOrderStatusCounts(userId);

        return IMOOCJSONResult.ok(result);
    }

    /**
     * 查询我的订单动向
     *
     * @return
     */
    @ApiOperation(value = "查询我的订单动向", notes = "查询我的订单动向", httpMethod = "POST")
    @PostMapping("/trend")
    public IMOOCJSONResult trend(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize) {

        // 判断输入是否为空
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        // 判断page是否为空，如果不为空，则设置为默认的1
        if (page == null) {
            page = 1;
        }

        // 设置默认pageSize
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询订单动向，具有分页
        PagedGridResult grid = myOrdersService.getOrdersTrend(userId, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }


}
