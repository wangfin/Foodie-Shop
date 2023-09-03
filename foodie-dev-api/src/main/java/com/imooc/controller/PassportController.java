package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.UserBO;
import com.imooc.pojo.vo.UsersVo;
import com.imooc.service.UserService;
import com.imooc.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "注册登录", tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 检测用户名是否存在
     *
     * @param username 用户名
     * @return
     */
    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username) {
        // @RequestParam 表示username是一种请求参数而不是请求路径
        // 这里使用的是apache commons-lang3中的StringUtils
        // 1. 判断用户名是否为空，或者空字符串
        if (StringUtils.isBlank(username)) {
            // 返回HTTP状态码，500
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }

        // 2. 查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }

        // 3. 请求成功，用户名没有重复
        return IMOOCJSONResult.ok();
    }

    /**
     * 用户注册
     *
     * @param userBO   信息BO
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPwd = userBO.getConfirmPassword();

        // 1. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(username) ||
                StringUtils.isBlank(username)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        // 2. 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        // 3. 密码长度不能少于6位
        if (password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }
        // 4. 判断两次密码是否一致
        if (!password.equals(confirmPwd)) {
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        // 5. 实现注册
        Users userResult = userService.createUser(userBO);

        // 删除信息
        // userResult = setNullProperty(userResult);

        // 实现用户的redis会话
        UsersVo usersVo = conventUsersVo(userResult);

        // 在注册完成之后，同样设置cookie
        CookieUtils.setCookie(request, response, BaseController.USER_COOKIE,
                JsonUtils.objectToJson(usersVo), true);

        // 同步购物车数据
        syncShopCartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok();
    }

    /**
     * 注册登录成功后，同步cookie和redis中的购物车数据
     *
     * @param userId 登录的userId
     */
    private void syncShopCartData(String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        /**
         * 1. redis中无数据，且cookie的中的购物车为空，那么不做处理，如果cookie购物车中有数据，则将cookie的数据放到redis中
         * 2. redis有数据，如果cookie中的购物车为空，那么直接把redis的购物车覆盖本地cookie，cookie中购物车不为空，如果cookie
         * 中的某个商品在redis中存在，则以cookie的为主，删除redis中的，把cookie中的商品直接覆盖redis中（参考京东，淘宝是必须登陆的）
         * 3. 同步到redis中后，覆盖本地cookie，保证本地数据是同步的
         */

        // 从redis中获取购物车
        String shopCartJsonRedis = redisUtils.get(FOODIE_SHOPCART_COOKIE + ":" + userId);

        // 从cookie中过去购物车
        String shopCartStrCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART_COOKIE, true);

        if (StringUtils.isBlank(shopCartJsonRedis)) {
            // redis为空，cookie不为空，直接把cookie中的数据放入redis
            if (StringUtils.isNotBlank(shopCartStrCookie)) {
                redisUtils.set(FOODIE_SHOPCART_COOKIE + ":" + userId, shopCartStrCookie);
            }
        } else {
            // redis不为空，cookie不为空，合并cookie和redis中的商品数据（同1个商品则覆盖redis）
            if (StringUtils.isNotBlank(shopCartStrCookie)) {

                /**
                 * 1. 已经存在的，把cookie中对应的数量，覆盖redis（参考京东）
                 * 2. 该项商品标记为待删除，统一放入一个待删除list
                 * 3. 从cookie中清理所有的待删除list
                 * 4. 合并redis和cookie中的数据
                 * 5. 更新到redis和cookie中
                 */
                List<ShopcartBO> shopCartListRedis = JsonUtils.jsonToList(shopCartJsonRedis, ShopcartBO.class);
                List<ShopcartBO> shopCartListCookie = JsonUtils.jsonToList(shopCartStrCookie, ShopcartBO.class);
                if (!CollectionUtils.isEmpty(shopCartListRedis) && !CollectionUtils.isEmpty(shopCartListCookie)) {

                    // 待删除列表
                    List<ShopcartBO> toBeRemovedShopCartList = new ArrayList<>();
                    for (ShopcartBO redisShopCart : shopCartListRedis) {
                        String redisSpecId = redisShopCart.getSpecId();

                        for (ShopcartBO cookieShopCart : shopCartListCookie) {
                            String cookieSpecId = cookieShopCart.getSpecId();

                            if (redisSpecId.equals(cookieSpecId)) {
                                // 覆盖redis中的购买数量
                                redisShopCart.setBuyCounts(cookieShopCart.getBuyCounts());
                                // 把cookie的数据放入待删除列表，用于最后的删除与合并
                                toBeRemovedShopCartList.add(cookieShopCart);
                            }
                        }
                    }

                    // 从现有cookie中删除对应的覆盖过的商品数据
                    shopCartListCookie.removeAll(toBeRemovedShopCartList);

                    // 合并两个list，主要是cookie中多的那一部分
                    shopCartListRedis.addAll(shopCartListCookie);

                    // 更新到redis和cookie
                    CookieUtils.setCookie(request, response, FOODIE_SHOPCART_COOKIE, JsonUtils.objectToJson(shopCartListRedis), true);
                    redisUtils.set(FOODIE_SHOPCART_COOKIE + ":" + userId, JsonUtils.objectToJson(shopCartListRedis));
                }

            } else {
                // redis不为空，cookie为空，直接把redis覆盖cookie
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART_COOKIE, shopCartJsonRedis, true);
            }
        }

    }

    /**
     * 用户登录
     *
     * @param userBO   信息BO
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 1. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(username) ||
                StringUtils.isBlank(username)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 2. 实现登录
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));

        if (userResult == null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }

        // 删除部分隐私信息
        // userResult = setNullProperty(userResult);

        // 实现用户的redis会话
        UsersVo usersVo = conventUsersVo(userResult);

        // 设置cookie，将用户登录信息保存在cookie里，而不是使用session
        // session后面使用分布式session，使用无状态信息
        // JsonUtils.objectToJson将userResult对象信息转换成字符串
        CookieUtils.setCookie(request, response, BaseController.USER_COOKIE,
                JsonUtils.objectToJson(usersVo), true);

        // 同步购物车数据
        syncShopCartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok(userResult);
    }

    // 将隐私信息删除，不显示
    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);

        return userResult;
    }

    /**
     * 用户退出登录
     *
     * @param userId   用户ID
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        // 清除用户相关信息的cookie
        CookieUtils.deleteCookie(request, response, USER_COOKIE);

        // 用户退出登录，需要清空购物车
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART_COOKIE);

        // 分布式会话中，用户退出登录需要清除用户redis数据
        redisUtils.delete(REDIS_USER_TOKEN + ":" + userId);

        return IMOOCJSONResult.ok();
    }
}
