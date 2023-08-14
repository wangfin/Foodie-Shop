package com.imooc.controller;

import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(value = "购物车接口", tags = {"购物车的相关接口"})
@RestController
@RequestMapping("shopcart")
public class ShopcartController extends BaseController {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 添加商品到购物车
     *
     * @param userId     用户ID
     * @param shopcartBO 购物车的BO
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 这里仅作简单判断
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        // 用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存
        // 需要判断当前购物车中包含已经存在的商品，如果存在则累加购买数量
        // 冒号之前的相同数据可以在可视化工具中作为一个文件夹的形式展示
        String shopCartJson = redisUtils.get(FOODIE_SHOPCART_COOKIE + ":" + userId);
        List<ShopcartBO> shopCartList = new ArrayList<>();
        if (StringUtils.isNotBlank(shopCartJson)) {
            // redis中已经有购物车了
            shopCartList = JsonUtils.jsonToList(shopCartJson, ShopcartBO.class);
            if (CollectionUtils.isEmpty(shopCartList)) {
                return null;
            }
            // 如果存在已有商品，如果有的话counts累加
            boolean isHaving = false;
            for (ShopcartBO sc: shopCartList) {
                String tmpSpecId = sc.getSpecId();
                if (tmpSpecId.equals(shopcartBO.getSpecId())) {
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if (!isHaving) {
                shopCartList.add(shopcartBO);
            }
        } else {
            // redis中没有购物车
            // 直接添加到购物车中
            shopCartList.add(shopcartBO);
        }

        // 覆盖现有redis中的购物车
        redisUtils.set(FOODIE_SHOPCART_COOKIE + ":" + userId, JsonUtils.objectToJson(shopCartList));

        return IMOOCJSONResult.ok();
    }

    /**
     * 从购物车中删除商品
     *
     * @param userId     用户ID
     * @param itemSpecId 商品规格ID
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 这里仅作简单判断
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        // 用户删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除Redis购物车中的数据
        String shopCartJson = redisUtils.get(FOODIE_SHOPCART_COOKIE + ":" + userId);
        if (StringUtils.isNotBlank(shopCartJson)) {
            // redis中已经有购物车
            List<ShopcartBO> shopCartList = JsonUtils.jsonToList(shopCartJson, ShopcartBO.class);
            if (CollectionUtils.isEmpty(shopCartList)) {
                return null;
            }
            // 判断购物车中是否有已经存在的商品，如果有则删除
            for (ShopcartBO sc: shopCartList) {
                String tmpSpecId = sc.getSpecId();
                if (tmpSpecId.equals(itemSpecId)) {
                    shopCartList.remove(sc);
                    break;
                }
            }
            // 覆盖现有redis中的购物车
            redisUtils.set(FOODIE_SHOPCART_COOKIE + ":" + userId, JsonUtils.objectToJson(shopCartList));
        }

        return IMOOCJSONResult.ok();
    }

}
