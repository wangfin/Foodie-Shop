package com.imooc.controller;

import com.imooc.pojo.Orders;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVo;
import com.imooc.service.center.MyOrdersService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.RedisUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.UUID;

@Controller
public class BaseController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MyOrdersService myOrdersService;

    public static final String FOODIE_SHOPCART_COOKIE = "shopcart";
    public static final String USER_COOKIE = "user";

    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    // 支付中心的调用地址
    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";		// produce

    // 微信支付成功 -> 支付中心 -> 天天吃货平台
    //                       |-> 回调通知的url
    String payReturnUrl = "http://api.z.mukewang.com/foodie-dev-api/orders/notifyMerchantOrderPaid";

    // 用户上传头像的位置
    public static final String IMAGE_USER_FACE_LOCATION = File.separator + "Java_demo" +
                                                            File.separator + "Foodie-dev" +
                                                            File.separator + "images" +
                                                            File.separator + "faces";

    /**
     * 用于验证用户和订单是否有关联关系，避免非法用户调用
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return
     */
    public IMOOCJSONResult checkUserOrder(String userId, String orderId){
        Orders order = myOrdersService.queryMyOrder(userId, orderId);
        if (order == null){
            return IMOOCJSONResult.errorMsg("订单不存在！");
        }else {
            return IMOOCJSONResult.ok();
        }
    }

    /**
     * 用户登录数据转换
     */
    public UsersVo conventUsersVo(Users user) {
        // 实现用户redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisUtils.set(REDIS_USER_TOKEN + ":" + user.getId(), uniqueToken);

        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(user, usersVo);
        usersVo.setUserUniqueToken(uniqueToken);
        return usersVo;
    }
}
