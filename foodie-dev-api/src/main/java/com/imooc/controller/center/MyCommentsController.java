package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.enums.YesOrNoEnum;
import com.imooc.pojo.OrderItems;
import com.imooc.pojo.Orders;
import com.imooc.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.service.center.MyCommentsService;
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

import java.util.List;

@Api(value = "用户中心-我的评价", tags = {"我的评价相关的api接口"})
@RestController
@RequestMapping("mycomments")
public class MyCommentsController extends BaseController {

    @Autowired
    private MyCommentsService myCommentsService;

    @Autowired
    private MyOrdersService myOrdersService;

    /**
     * 显示订单的关联产品
     * @return
     */
    @ApiOperation(value = "显示订单的关联产品", notes = "显示订单的关联产品", httpMethod = "POST")
    // 这里的参数不是路径参数了，是请求参数
    @PostMapping("/pending")
    public IMOOCJSONResult pending(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId){

        // 判断用户和订单是否关联
        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()){
            return checkResult;
        }
        // 判断该笔订单是否已经评价过，评价过了就不在继续
        Orders myOrder = (Orders)checkResult.getData();
        if (myOrder.getIsComment() == YesOrNoEnum.YES.type){
            return IMOOCJSONResult.errorMsg("该笔订单已经评价！");
        }

        List<OrderItems> list = myCommentsService.queryPendingComment(orderId);

        return IMOOCJSONResult.ok(list);

    }

    /**
     * 用于验证用户和订单是否有关联关系，避免非法用户调用
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return
     */
    private IMOOCJSONResult checkUserOrder(String userId, String orderId){
        Orders order = myOrdersService.queryMyOrder(userId, orderId);
        if (order == null){
            return IMOOCJSONResult.errorMsg("订单不存在！");
        }else {
            return IMOOCJSONResult.ok(order);
        }
    }

    /**
     * 保存商品评价信息
     * @return
     */
    @ApiOperation(value = "保存商品评价信息", notes = "保存商品评价信息", httpMethod = "POST")
    // 这里的参数不是路径参数了，是请求参数
    @PostMapping("/saveList")
    public IMOOCJSONResult saveList(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @RequestParam String orderId,
            @ApiParam(name = "commentList", value = "商品评价信息的BO", required = true)
            @RequestBody List<OrderItemsCommentBO> commentList){

        // 判断用户和订单是否关联
        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()){
            return checkResult;
        }

        // 判断评论内容list不能为空
        if (commentList == null || commentList.isEmpty()){
            return IMOOCJSONResult.errorMsg("评论内容不能为空！");
        }

        myCommentsService.saveComments(userId, orderId, commentList);

        return IMOOCJSONResult.ok();

    }

    /**
     * 查询我的评价列表
     * @return
     */
    @ApiOperation(value = "查询我的评价列表", notes = "查询我的评价列表", httpMethod = "POST")
    @PostMapping("/query")
    public IMOOCJSONResult query(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize){

        // 判断输入是否为空
        if (StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // 判断page是否为空，如果不为空，则设置为默认的1
        if (page == null){
            page = 1;
        }

        // 设置默认pageSize
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询我的历史评价
        PagedGridResult grid = myCommentsService.queryMyComments(userId, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }


}
