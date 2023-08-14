package com.imooc.controller;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.PayMethodEnum;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.MerchantOrdersVO;
import com.imooc.pojo.vo.OrderVO;
import com.imooc.service.AddressService;
import com.imooc.service.OrderService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "订单相关", tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 创建订单
     *
     * @param submitOrderBO 订单BO
     * @return
     */
    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public IMOOCJSONResult create(
            @ApiParam(name = "submitOrderBO", value = "提交订单的BO", required = true)
            @RequestBody SubmitOrderBO submitOrderBO,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 判断支付方式
        if (!submitOrderBO.getPayMethod().equals(PayMethodEnum.WEIXIN.type) &&
                !submitOrderBO.getPayMethod().equals(PayMethodEnum.ALIPAY.type)) {
            return IMOOCJSONResult.errorMsg("支付方式不支持！");
        }

        // System.out.println(submitOrderBO.toString());

        // 判断redis中的购物车是否有数据
        String shopCartJson = redisUtils.get(FOODIE_SHOPCART_COOKIE + ":" + submitOrderBO.getUserId());
        if (StringUtils.isBlank(shopCartJson)) {
            return IMOOCJSONResult.errorMsg("购物车数据异常！");
        }
        List<ShopcartBO> shopCartList = JsonUtils.jsonToList(shopCartJson, ShopcartBO.class);

        // 1. 创建订单
        OrderVO orderVO = orderService.createOrder(shopCartList, submitOrderBO);
        String orderId = orderVO.getOrderId();

        // 2. 创建订单以后，移除购物车中已结算（已提交）的商品

        /*
         * 1001
         * 2002 -> 用户购买
         * 3003 -> 用户购买
         * 4004
         */
        // TODO 整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie

        // 把cookie设置为“”
        //  CookieUtils.setCookie(request, response, BaseController.FOODIE_SHOPCART_COOKIE, "", true);

        // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        // 设置返回地址
        merchantOrdersVO.setReturnUrl(payReturnUrl);

        // 方便测试，将金额全部改成1分钱
        merchantOrdersVO.setAmount(1);

        // 设置Rest风格的请求，调用支付中心的程序
        // 首先设置http的headers
        HttpHeaders headers = new HttpHeaders();
        // 使用json
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 传入支付中心的账户和密码
        headers.add("imoocUserId", "imooc");
        headers.add("password", "imooc");

        // 发起请求
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO, headers);
        ResponseEntity<IMOOCJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl, entity, IMOOCJSONResult.class);
        // 获取里面的返回值
        IMOOCJSONResult paymentResult = responseEntity.getBody();
        // 如果不成功
        if (paymentResult.getStatus() != 200) {
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败");
        }

        return IMOOCJSONResult.ok(orderId);
    }

    /**
     * notifyMerchantOrderPaid 回调函数，在使用微信付款之后，支付中心会调用这个函数，修改订单支付状态
     * 付款完成之后将订单的付款状态改为已付款，待发货
     *
     * @param merchantOrderId 订单号
     * @return
     */
    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(
            @ApiParam(name = "merchantOrderId", value = "商户订单ID", required = true)
            @RequestParam String merchantOrderId) {
        // 修改订单状态，这里是wait pay
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();

    }

    /**
     * 在支付页面轮询获取订单的支付情况，主要是获取到20已支付状态就回传信息
     *
     * @param orderId 订单号
     * @return
     */
    @PostMapping("getPaidOrderInfo")
    public IMOOCJSONResult getPaidOrderInfo(
            @ApiParam(name = "orderId", value = "用户订单ID", required = true)
            @RequestParam String orderId) {
        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return IMOOCJSONResult.ok(orderStatus);
    }

}
